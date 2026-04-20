<template>
  <div class="editor-wrapper">
    <textarea
        ref="editorRef"
        :value="content"
        class="editor"
        placeholder="开始编辑..."
        @input="onInput"
        @keydown="emit('keydown')"
        @select="emit('select')"
        @click="emit('select')"
        :disabled="!canEdit"
        :class="{ 'readonly-editor': !canEdit }"
    ></textarea>

    <div class="info-bar">
      <div class="cursor-position">行 {{ cursorLine }},列{{ cursorColumn }}</div>
      <div class="word-count">
        {{ selectedWordCount > 0 ? `已选中 ${selectedWordCount} 字` : `总字数 ${wordCount}` }}
      </div>
      <div class="tips">💡 提示：打开多个窗口测试实时同步</div>
    </div>
  </div>
</template>

<script setup>
import {ref} from "vue";

const props = defineProps({
  content: String,
  canEdit: Boolean,
  cursorLine: Number,
  cursorColumn: Number,
  selectedWordCount: Number,
  wordCount: Number
})

const emit = defineEmits(['input', 'keydown', 'select', 'update:content'])

const editorRef = ref(null)

const onInput = (e) => {
  emit('update:content',e.target.value)
  emit('input')
}

const onKeydown = () => {
  emit('keydown')
}

const onSelect = () => {
  emit('select')
}

//暴露editorRef给父组件
defineExpose({
  editorRef
})
</script>

<style scoped>
.editor-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.editor {
  flex: 1;
  width: 100%;
  padding: 20px;
  font-size: 15px;
  font-family: 'Monaco','Menlo','Ubuntu Mono','monospace';
  line-height: 1.6;
  border: none;
  resize: none;
  outline: none;
  background: white;
}
.editor:focus {
  outline: none;
}
.readonly-editor {
  background: #f5f5f5;
  cursor: not-allowed;
}
.info-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 24px;
  background: #fafafa;
  border-top: 1px solid #e0e0e0;
  font-size: 12px;
  color: #888;
}
.cursor-position, .word-count {
  font-family: monospace;
}
.tips {
  color: #aaa;
}
</style>