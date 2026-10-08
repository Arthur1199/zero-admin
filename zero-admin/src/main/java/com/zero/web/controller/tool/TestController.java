package com.zero.web.controller.tool;

import com.zero.common.core.controller.BaseController;
import com.zero.common.core.domain.R;
import com.zero.common.utils.StringUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: swagger 用户测试方法
 */
@Tag(name = "用户信息管理", description = "用户相关API接口")
@RestController
@RequestMapping("/test/user")
public class TestController extends BaseController
{
    /**
     * users字段
     */
    private final static Map<Integer, UserEntity> users = new LinkedHashMap<Integer, UserEntity>();
    {
        users.put(1, new UserEntity(1, "admin", "admin123", "15888888888"));
        users.put(2, new UserEntity(2, "ry", "admin123", "15666666666"));
    }

    @Operation(summary = "获取用户列表", description = "获取所有用户的信息列表")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成功获取用户列表",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserEntity.class))),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 用户列表
     * @return
     * 返回值: R<List<UserEntity>>
     * 返回值说明：用户列表
     */
    @GetMapping("/list")
    public R<List<UserEntity>> userList()
    {
        List<UserEntity> userList = new ArrayList<UserEntity>(users.values());
        return R.ok(userList);
    }

    @Operation(summary = "获取用户详细", description = "根据用户ID获取用户详细信息")
    @GetMapping("/{userId}")
    public R<UserEntity> getUser(
            @Parameter(name = "userId", description = "用户ID", required = true,
                    example = "1", schema = @Schema(type = "integer"))
            @PathVariable Integer userId)
    {
        if (!users.isEmpty() && users.containsKey(userId))
        {
            return R.ok(users.get(userId));
        }
        else
        {
            return R.fail("用户不存在");
        }
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: save
     * @param user 用户参数
     * @return
     * 返回值: R<String>
     * 返回值说明：save
     */

    @Operation(summary = "新增用户", description = "创建新用户")
    @PostMapping("/save")
    public R<String> save(@RequestBody UserEntity user)
    {
        if (StringUtils.isNull(user) || StringUtils.isNull(user.getUserId()))
        {
            return R.fail("用户ID不能为空");
        }
        users.put(user.getUserId(), user);
        return R.ok();
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 修改
     * @param user 用户参数
     * @return
     * 返回值: R<String>
     * 返回值说明：修改
     */

    @Operation(summary = "更新用户", description = "更新现有用户信息")
    @PutMapping("/update")
    public R<String> update(@RequestBody UserEntity user)
    {
        if (StringUtils.isNull(user) || StringUtils.isNull(user.getUserId()))
        {
            return R.fail("用户ID不能为空");
        }
        if (users.isEmpty() || !users.containsKey(user.getUserId()))
        {
            return R.fail("用户不存在");
        }
        users.remove(user.getUserId());
        users.put(user.getUserId(), user);
        return R.ok();
    }

    @Operation(summary = "删除用户信息", description = "根据用户ID删除用户")
    @DeleteMapping("/{userId}")
    public R<String> delete(
            @Parameter(name = "userId", description = "用户ID", required = true,
                    example = "1", schema = @Schema(type = "integer"))
            @PathVariable Integer userId)
    {
        if (!users.isEmpty() && users.containsKey(userId))
        {
            users.remove(userId);
            return R.ok();
        }
        else
        {
            return R.fail("用户不存在");
        }
    }
}
/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 用户Entity类用户Entity
 */

@io.swagger.v3.oas.annotations.media.Schema(name = "UserEntity", description = "用户实体")
class UserEntity
{
    /**
     * 用户ID字段
     */
    @io.swagger.v3.oas.annotations.media.Schema(description = "用户ID", example = "1", type = "integer")
    private Integer userId;
    /**
     * username字段
     */

    @io.swagger.v3.oas.annotations.media.Schema(description = "用户名称", example = "admin", type = "string")
    private String username;
    /**
     * 密码字段
     */

    @io.swagger.v3.oas.annotations.media.Schema(description = "用户密码", example = "admin123", type = "string")
    private String password;
    /**
     * mobile字段
     */

    @io.swagger.v3.oas.annotations.media.Schema(description = "用户手机", example = "15888888888", type = "string")
    private String mobile;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 07:16
     * 说明: 用户测试实体无参构造方法
     * @return
     * 返回值: UserEntity
     * 返回值说明：创建空的用户测试实体
     */
    public UserEntity()
    {

    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 07:16
     * 说明: 用户测试实体全参构造方法
     * @param userId 用户ID
     * @param username 用户名称
     * @param password 用户密码，仅用于Swagger测试数据
     * @param mobile 用户手机号码
     * @return
     * 返回值: UserEntity
     * 返回值说明：创建包含测试字段的用户实体
     */
    public UserEntity(Integer userId, String username, String password, String mobile)
    {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.mobile = mobile;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取用户ID
     * @return
     * 返回值: Integer
     * 返回值说明：获取用户ID
     */

    public Integer getUserId()
    {
        return userId;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置用户ID
     * @param userId 用户ID参数
     * @return
     * 返回值: void
     * 返回值说明：设置用户ID
     */

    public void setUserId(Integer userId)
    {
        this.userId = userId;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Username
     * @return
     * 返回值: String
     * 返回值说明：获取Username
     */

    public String getUsername()
    {
        return username;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Username
     * @param username username参数
     * @return
     * 返回值: void
     * 返回值说明：设置Username
     */

    public void setUsername(String username)
    {
        this.username = username;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取密码
     * @return
     * 返回值: String
     * 返回值说明：获取密码
     */

    public String getPassword()
    {
        return password;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置密码
     * @param password 密码参数
     * @return
     * 返回值: void
     * 返回值说明：设置密码
     */

    public void setPassword(String password)
    {
        this.password = password;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Mobile
     * @return
     * 返回值: String
     * 返回值说明：获取Mobile
     */

    public String getMobile()
    {
        return mobile;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Mobile
     * @param mobile mobile参数
     * @return
     * 返回值: void
     * 返回值说明：设置Mobile
     */

    public void setMobile(String mobile)
    {
        this.mobile = mobile;
    }
}
