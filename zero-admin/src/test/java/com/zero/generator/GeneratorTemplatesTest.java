package com.zero.generator;

import com.zero.generator.domain.GenTable;
import com.zero.generator.domain.GenTableColumn;
import com.zero.generator.mapper.GenTableMapper;
import com.zero.generator.service.GenTableServiceImpl;
import com.zero.generator.util.VelocityUtils;
import java.io.ByteArrayInputStream;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/16 15:42
 * 说明: 隔离验证真实生成Service、13个classpath模板及生成Java/XML，不建表、不执行生成SQL
 */
class GeneratorTemplatesTest
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 16:00
     * 说明: 历史空前端类型采用当前Vue3模板，显式Vue2保留，非法类型不能静默生成错误页面
     * @return
     * 返回值: void
     * 返回值说明: 校验前端类型选择规则，不修改数据库生成配置
     */
    @Test
    void defaultsToVue3AndKeepsExplicitVue2()
    {
        assertThat(VelocityUtils.getTemplateList("crud", null)).contains("vm/vue/v3/index.vue.vm");
        assertThat(VelocityUtils.getTemplateList("crud", "")).contains("vm/vue/v3/index.vue.vm");
        assertThat(VelocityUtils.getTemplateList("tree", "element-ui")).contains("vm/vue/index-tree.vue.vm");
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> VelocityUtils.getTemplateList("crud", "unsupported"))
                .isInstanceOf(com.zero.common.exception.ServiceException.class);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 15:42
     * 说明: 验证六类生成配置的预览、ZIP和批量ZIP一致，编译Java并解析MyBatis映射，保留前端验收产物
     * @param category 单表crud、树表tree、主子表sub
     * @param webType Vue2 element-ui或Vue3 element-plus
     * @return
     * 返回值: void
     * 返回值说明: 全部验证仅使用内存元数据与target目录，不连接数据库或启动业务服务
     */
    @ParameterizedTest
    @CsvSource({"crud,element-ui", "tree,element-ui", "sub,element-ui", "crud,element-plus", "tree,element-plus", "sub,element-plus"})
    void previewsAndDownloadsCompile(String category, String webType) throws Exception
    {
        GenTable table = table(category, webType);
        GenTableMapper mapper = mock(GenTableMapper.class);
        when(mapper.selectGenTableById(1L)).thenReturn(table);
        when(mapper.selectGenTableByName(table.getTableName())).thenReturn(table);
        when(mapper.selectGenTableByName("fixture_line")).thenReturn(table.getSubTable());
        GenTableServiceImpl service = new GenTableServiceImpl();
        ReflectionTestUtils.setField(service, "genTableMapper", mapper);
        Map<String, String> preview = service.previewCode(1L);
        assertThat(preview).hasSize(category.equals("sub") ? 10 : 9);
        Map<String, String> expected = new LinkedHashMap<>();
        Path output = Path.of("target", "generated-template-check", category + "-" + webType);
        for (Map.Entry<String, String> entry : preview.entrySet())
        {
            String name = VelocityUtils.getFileName(entry.getKey(), table);
            assertThat(name).isNotBlank();
            assertThat(entry.getValue()).doesNotContain("com.ruoyi", "javax.servlet", "@PreAuthorize", "${ClassName}", "${pkColumn", "#foreach", "#if(");
            expected.put(name, entry.getValue());
            Path file = output.resolve(name);
            Files.createDirectories(file.getParent());
            Files.writeString(file, entry.getValue(), StandardCharsets.UTF_8);
        }
        assertThat(unzip(service.downloadCode(table.getTableName()))).isEqualTo(expected);
        assertThat(unzip(service.downloadCode(new String[] {table.getTableName()}))).isEqualTo(expected);
        String sql = expected.get(table.getBusinessName() + "Menu.sql");
        assertThat(sql).doesNotContain(":query", "sys_role_menu");
        String controller = expected.values().stream().filter(s -> s.contains("public class " + table.getClassName() + "Controller")).findFirst().orElseThrow();
        assertThat(controller).contains("@RequiresPermissions", "jakarta.servlet.http.HttpServletResponse");
        if (webType.equals("element-plus"))
        {
            String vue = expected.get("vue/views/templatecheck/" + table.getBusinessName() + "/index.vue");
            assertThat(vue).contains("export default", "dicts:", "v-model=\"open\"", "YYYY-MM-DD")
                    .doesNotContain(".sync=", ".native=", "slot-scope", "@riophae", "proxy.useDict", "script setup");
        }
        compileAndParse(output, table);
        verify(mapper, never()).insertGenTable(any());
        verify(mapper, never()).updateGenTable(any());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 15:42
     * 说明: 编译真实生成的Java并用项目MyBatis解析真实XML，不运行任何SQL
     * @param output 当前场景的隔离生成目录
     * @param table 包含主子表信息的内存表定义
     * @return
     * 返回值: void
     * 返回值说明: Java编译及XML映射解析失败会直接终止测试
     */
    private void compileAndParse(Path output, GenTable table) throws Exception
    {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertThat(compiler).isNotNull();
        List<Path> sources;
        try (var paths = Files.walk(output.resolve("main/java")))
        {
            sources = paths.filter(p -> p.toString().endsWith(".java")).toList();
        }
        Path classes = output.resolve("classes");
        Files.createDirectories(classes);
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        try (var manager = compiler.getStandardFileManager(diagnostics, null, StandardCharsets.UTF_8))
        {
            var units = manager.getJavaFileObjectsFromPaths(sources);
            String classpath = System.getProperty("surefire.test.class.path", System.getProperty("java.class.path"));
            boolean success = compiler.getTask(null, manager, diagnostics,
                    List.of("--release", "17", "-encoding", "UTF-8", "-classpath", classpath, "-d", classes.toString()), null, units).call();
            assertThat(success).withFailMessage("生成Java编译失败: %s", diagnostics.getDiagnostics()).isTrue();
        }
        ClassLoader previous = Thread.currentThread().getContextClassLoader();
        try (var loader = new URLClassLoader(new java.net.URL[] {classes.toUri().toURL()}, previous))
        {
            Thread.currentThread().setContextClassLoader(loader);
            Configuration config = new Configuration();
            config.getTypeAliasRegistry().registerAlias(table.getClassName(), loader.loadClass(table.getPackageName() + ".domain." + table.getClassName()));
            if (table.isSub()) config.getTypeAliasRegistry().registerAlias("FixtureLine", loader.loadClass(table.getPackageName() + ".domain.FixtureLine"));
            Path xml = output.resolve("main/resources/mapper/templatecheck/" + table.getClassName() + "Mapper.xml");
            try (var input = Files.newInputStream(xml))
            {
                new XMLMapperBuilder(input, config, xml.toString(), config.getSqlFragments()).parse();
            }
            assertThat(config.hasStatement(table.getPackageName() + ".mapper." + table.getClassName() + "Mapper.select" + table.getClassName() + "List")).isTrue();
        }
        finally
        {
            Thread.currentThread().setContextClassLoader(previous);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 15:42
     * 说明: 读取内存ZIP内容用于逐文件比对，不解压到业务源码目录
     * @param bytes 真实Service返回的ZIP字节
     * @return
     * 返回值: Map<String, String>
     * 返回值说明: ZIP内相对路径与UTF8内容，禁止重复文件名
     */
    private Map<String, String> unzip(byte[] bytes) throws Exception
    {
        Map<String, String> entries = new LinkedHashMap<>();
        try (var zip = new ZipInputStream(new ByteArrayInputStream(bytes), StandardCharsets.UTF_8))
        {
            java.util.zip.ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null)
            {
                assertThat(entries).doesNotContainKey(entry.getName());
                entries.put(entry.getName(), new String(zip.readAllBytes(), StandardCharsets.UTF_8));
            }
        }
        return entries;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 15:42
     * 说明: 构造覆盖字典、数值、日期区间、上传、富文本和子表的隔离元数据
     * @param category 模板类别
     * @param webType 前端类型
     * @return
     * 返回值: GenTable
     * 返回值说明: 不对应任何真实数据库表的生成配置
     */
    private GenTable table(String category, String webType)
    {
        GenTable table = new GenTable();
        table.setTableId(1L);
        table.setTableName("fixture_" + category);
        table.setClassName("Fixture" + Character.toUpperCase(category.charAt(0)) + category.substring(1));
        table.setBusinessName("fixture" + Character.toUpperCase(category.charAt(0)) + category.substring(1));
        table.setPackageName("com.zero.templatecheck");
        table.setModuleName("templatecheck");
        table.setFunctionName("隔离验收" + category);
        table.setFunctionAuthor("Arthur 亚瑟");
        table.setTplCategory(category);
        table.setTplWebType(webType);
        table.setOptions("{\"treeCode\":\"id\",\"treeParentCode\":\"parent_id\",\"treeName\":\"title\",\"parentMenuId\":\"3\"}");
        List<GenTableColumn> columns = new ArrayList<>();
        columns.add(column("id", "Long", "input"));
        columns.get(0).setIsPk("1");
        columns.get(0).setIsIncrement("1");
        columns.get(0).setIsQuery("0");
        columns.add(column("title", "String", "input"));
        columns.get(1).setIsRequired("1");
        columns.add(column("status", "Integer", "radio"));
        columns.get(2).setDictType("sys_normal_disable");
        columns.add(column("tags", "String", "checkbox"));
        columns.get(3).setDictType("sys_normal_disable");
        columns.add(column("amount", "BigDecimal", "input"));
        columns.add(column("event_time", "Date", "datetime"));
        columns.get(5).setQueryType("BETWEEN");
        columns.add(column("image", "String", "imageUpload"));
        columns.add(column("files", "String", "fileUpload"));
        columns.add(column("content", "String", "editor"));
        columns.add(column("create_time", "Date", "datetime"));
        columns.add(column("update_time", "Date", "datetime"));
        if (category.equals("tree")) columns.add(column("parent_id", "Long", "input"));
        table.setColumns(columns);
        if (category.equals("sub"))
        {
            GenTable child = new GenTable();
            child.setTableName("fixture_line");
            child.setClassName("FixtureLine");
            child.setFunctionName("验收明细");
            child.setColumns(List.of(column("line_id", "Long", "input"), column("parent_id", "Long", "input"),
                    column("title", "String", "input"), column("event_time", "Date", "datetime"), column("status", "Integer", "select")));
            child.getColumns().get(0).setIsPk("1");
            child.getColumns().get(0).setIsIncrement("1");
            child.getColumns().get(4).setDictType("sys_normal_disable");
            table.setSubTableName("fixture_line");
            table.setSubTableFkName("parent_id");
            table.setSubTable(child);
        }
        return table;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/16 15:42
     * 说明: 生成具有完整默认标记的字段，避免测试依赖数据库初始化
     * @param name 数据库字段名
     * @param javaType Java类型
     * @param htmlType 表单控件类型
     * @return
     * 返回值: GenTableColumn
     * 返回值说明: 默认允许查询、插入、编辑和列表展示的测试字段
     */
    private GenTableColumn column(String name, String javaType, String htmlType)
    {
        GenTableColumn column = new GenTableColumn();
        column.setColumnName(name);
        column.setColumnComment(name.equals("title") ? "名称" : name);
        column.setJavaField(com.zero.common.utils.StringUtils.toCamelCase(name));
        column.setJavaType(javaType);
        column.setHtmlType(htmlType);
        column.setDictType("");
        column.setIsPk("0");
        column.setIsIncrement("0");
        column.setIsRequired("0");
        column.setIsInsert("1");
        column.setIsEdit("1");
        column.setIsList("1");
        column.setIsQuery("1");
        column.setQueryType("EQ");
        return column;
    }
}
