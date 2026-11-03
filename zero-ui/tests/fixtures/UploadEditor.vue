<template>
  <main style="padding: 20px">
    <section id="editor-section">
      <h2>富文本</h2>
      <button type="button" @click="showEditor = !showEditor">{{ showEditor ? '卸载富文本' : '恢复富文本' }}</button>
      <Editor v-if="showEditor" v-model="content" :min-height="160" :file-size="1" />
      <output data-testid="editor-model">{{ content }}</output>
    </section>
    <section id="file-section">
      <h2>文件上传</h2>
      <FileUpload v-model="files" :file-size="1" :file-type="['txt']" :limit="3" />
      <output data-testid="file-model">{{ files }}</output>
    </section>
    <section id="image-section">
      <h2>图片上传</h2>
      <ImageUpload v-model="images" :file-size="1" :limit="3" />
      <output data-testid="image-model">{{ images }}</output>
    </section>
  </main>
</template>
<script>
import Editor from '../../src/components/Editor/index.vue'
import FileUpload from '../../src/components/FileUpload/index.vue'
import ImageUpload from '../../src/components/ImageUpload/index.vue'

export default {
  components: { Editor, FileUpload, ImageUpload },
  data() { return { content: '', files: '', images: '', showEditor: true } }
}
</script>
