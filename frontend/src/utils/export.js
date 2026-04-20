//导出为文本文件
import {content} from "jsdom/lib/generated/css-property-descriptors.js";

export const downloadFile = (content, filename, type = 'text/plain') => {
    const blob = new Blob([content],{type})
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download =filename
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
}

//导出为Markdown
export const exportAsMarkdown = (title,content) => {
    const mdContent = `# ${title}\n\n${content}`
    downloadFile(mdContent,`${title}.md`,'text/markdown')
}

//导出为TXT
export const exportAsTxt = (title,content) => {
    downloadFile(content,`${title}.txt`,'text/plain')
}

//导出为HTML
export const exportAsHtml = (title,content) => {
    const htmlContent = `<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>${title}</title>
  <style>
    body { max-width: 800px; margin: 0 auto; padding: 40px 20px; font-family: system-ui; line-height: 1.6; }
    pre { background: #f5f5f5; padding: 16px; border-radius: 8px; overflow-x: auto; }
  </style>
</head>
<body>
  <h1>${title}</h1>
  <div>${content.replace(/\n/g, '<br>')}</div>
</body>
</html>`
    downloadFile(htmlContent, `${title}.html`, 'text/html')
}

//导出为JSON
export const exportAsJson = (doc) => {
    const jsonContent = JSON.stringify({
        id:doc.id,
        title:doc.title,
        content: doc.content,
        createTime:doc.createTime,
        updateTime: doc.updateTime
    },null,2)
    downloadFile(jsonContent,`${doc.title}.json`,'application/json')
}