package com.collabdoc.controller;

import com.collabdoc.entity.Document;
import com.collabdoc.service.DocumentService;
import com.collabdoc.utils.JwtUtil;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import java.io.ByteArrayOutputStream;

import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;

import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;

@RestController
@RequestMapping("/api/export")
public class ExportController {
    private final DocumentService documentService;
    private final JwtUtil jwtUtil;

    private ExportController(DocumentService documentService,JwtUtil jwtUtil){
        this.documentService = documentService;
        this.jwtUtil = jwtUtil;
    }

    //导出为TXT
    @GetMapping("/txt/{docId}")
    public void exportAsTxt(@PathVariable String docId,
                            @RequestHeader("Authorization") String authHeader,
                            HttpServletResponse response) throws IOException {
        String userId = getUserId(authHeader);
        Document doc = documentService.getDocumentById(docId);

        if (doc == null){
            response.setStatus(404);
            response.getWriter().write("文档不存在");
            return;
        }

        response.setContentType("text/plain;charset=UTF-8");
        response.setHeader("Content-Disposition","attachment;filename="+ URLEncoder.encode(doc.getTitle() + ".txt","UTF-8"));
        response.getWriter().write(doc.getContent() == null ? "" : doc.getContent());
    }

    //导出为Markdown
    @GetMapping("/md/{docId}")
    public void exportAsMarkdown(@PathVariable String docId,
                                 @RequestHeader("Authorization") String authHeader,
                                 HttpServletResponse response) throws IOException {
        String userId = getUserId(authHeader);
        Document doc = documentService.getDocumentById(docId);

        if (doc == null){
            response.setStatus(404);
            return;
        }

        String content = "# " + doc.getTitle() + "\n\n" + (doc.getContent() == null ? "" : doc.getContent());
        response.setContentType("text/markdown;charset=UTF-8");
        response.setHeader("Content-Disposition","attachment;filename=" + URLEncoder.encode(doc.getTitle() + ".md","UTF-8"));
        response.getWriter().write(content);
    }

    //导出为HTML
    @GetMapping("/html/{docId}")
    public void exportAsHtml(@PathVariable String docId,
                                 @RequestHeader("Authorization") String authHeader,
                                 HttpServletResponse response) throws IOException {
        String userId = getUserId(authHeader);
        Document doc = documentService.getDocumentById(docId);

        if (doc == null){
            response.setStatus(404);
            return;
        }

        String html = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "  <meta charset=\"UTF-8\">\n" +
                "  <title>" + escapeHtml(doc.getTitle()) + "</title>\n" +
                "  <style>\n" +
                "    body { max-width: 800px; margin: 0 auto; padding: 40px 20px; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; line-height: 1.6; }\n" +
                "    pre { background: #f5f5f5; padding: 16px; border-radius: 8px; overflow-x: auto; }\n" +
                "  </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "  <h1>" + escapeHtml(doc.getTitle()) + "</h1>\n" +
                "  <div>" + escapeHtml(doc.getContent() == null ? "" : doc.getContent()).replace("\n", "<br>") + "</div>\n" +
                "</body>\n" +
                "</html>";
        response.setContentType("text/html;charset=UTF-8");
        response.setHeader("Content-Disposition","attachment;filename=" + URLEncoder.encode(doc.getTitle() + ".html","UTF-8"));
        response.getWriter().write(html);
    }

    //导出为PDF（使用 iText 5）
    /*@GetMapping("/pdf/{docId}")
    public void exportAsPdf(@PathVariable String docId,
                                 @RequestHeader("Authorization") String authHeader,
                                 HttpServletResponse response) throws IOException {
        try {
            String userId = getUserId(authHeader);
            com.collabdoc.entity.Document doc = documentService.getDocumentById(docId);

            if (doc == null){
                response.setStatus(404);
                response.getWriter().write("文档不存在");
                return;
            }

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition","attachment;filename=" + URLEncoder.encode(doc.getTitle() + ".pdf","UTF-8"));

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            //创建 PDF 文档
            com.itextpdf.text.Document pdfDoc = new com.itextpdf.text.Document(PageSize.A4);
            com.itextpdf.text.pdf.PdfWriter.getInstance(pdfDoc,baos);
            pdfDoc.open();

            //设置中文字体
            com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(
                    Font.FontFamily.HELVETICA,18, Font.BOLD
            );
            com.itextpdf.text.Font contentFont = new com.itextpdf.text.Font(
                    Font.FontFamily.HELVETICA,11
            );

            //添加标题
            com.itextpdf.text.Paragraph titlePara = new com.itextpdf.text.Paragraph(doc.getTitle(),titleFont);
            titlePara.setSpacingAfter(20);
            pdfDoc.add(titlePara);

            //添加内容
            String content = doc.getContent() == null ? "" : doc.getContent();
            for (String line : content.split("\n")){
                com.itextpdf.text.Paragraph linePara = new com.itextpdf.text.Paragraph(line,contentFont);
                linePara.setSpacingAfter(5);
                pdfDoc.add(linePara);
            }

            //添加导出时间
            pdfDoc.add(new com.itextpdf.text.Paragraph(" "));
            pdfDoc.add(new com.itextpdf.text.Paragraph("导出时间：" + new java.util.Date(),new com.itextpdf.text.Font(Font.FontFamily.HELVETICA,8)));

            pdfDoc.close();

            response.getOutputStream().write(baos.toByteArray());
            }catch (Exception e){
            e.printStackTrace();
            response.setStatus(500);
            response.getWriter().write("PDF 生成失败：" + e.getMessage());
        }
    }
*/


/*    @GetMapping("/pdf/{docId}")
    public void exportAsPdf(@PathVariable String docId,
                            @RequestHeader("Authorization") String authHeader,
                            HttpServletResponse response) throws IOException {
        try {
            String userId = getUserId(authHeader);
            com.collabdoc.entity.Document doc = documentService.getDocumentById(docId);

            if (doc == null) {
                response.setStatus(404);
                response.getWriter().write("文档不存在");
                return;
            }

            // 构建 HTML 内容
            String content = doc.getContent() == null ? "" : doc.getContent();
            String html = buildPdfHtml(doc.getTitle(), content);

            // 生成 PDF
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(html, null);
            builder.toStream(baos);
            builder.run();

            // 输出 PDF
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + URLEncoder.encode(doc.getTitle() + ".pdf", "UTF-8"));
            response.getOutputStream().write(baos.toByteArray());

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500);
            response.getWriter().write("PDF生成失败: " + e.getMessage());
        }
    }

    private String buildPdfHtml(String title, String content) {
        // 转义 HTML 特殊字符
        String escapedContent = content
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");

        // 将换行符转换为 <br/>
        String htmlContent = escapedContent.replace("\n", "<br/>");

        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "  <meta charset=\"UTF-8\"></meta>\n" +
                "  <title>" + title + "</title>\n" +
                "  <style>\n" +
                "    @font-face {\n" +
                "      font-family: 'NotoSans';\n" +
                "      src: url('file:/c:/windows/fonts/msyh.ttc');\n" +
                "    }\n" +
                "    body {\n" +
                "      font-family: 'NotoSans', 'Microsoft YaHei', 'SimHei', '宋体', sans-serif;\n" +
                "      margin: 50px;\n" +
                "      line-height: 1.6;\n" +
                "    }\n" +
                "    h1 {\n" +
                "      text-align: center;\n" +
                "      font-size: 20px;\n" +
                "      margin-bottom: 30px;\n" +
                "    }\n" +
                "    .content {\n" +
                "      font-size: 12px;\n" +
                "    }\n" +
                "    .footer {\n" +
                "      margin-top: 50px;\n" +
                "      font-size: 10px;\n" +
                "      color: #999;\n" +
                "      text-align: center;\n" +
                "    }\n" +
                "  </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "  <h1>" + title + "</h1>\n" +
                "  <div class=\"content\">" + htmlContent + "</div>\n" +
                "  <div class=\"footer\">导出时间: " + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()) + "</div>\n" +
                "</body>\n" +
                "</html>";
    }*/

    //导出为JSON
    @GetMapping("/json/{docId}")
    public void exportAsJson(@PathVariable String docId,
                                 @RequestHeader("Authorization") String authHeader,
                                 HttpServletResponse response) throws IOException {
        String userId = getUserId(authHeader);
        com.collabdoc.entity.Document doc = documentService.getDocumentById(docId);

        if (doc == null){
            response.setStatus(404);
            return;
        }

        String json = String.format("{\n" +
                        "  \"id\": \"%s\",\n" +
                        "  \"title\": \"%s\",\n" +
                        "  \"content\": \"%s\",\n" +
                        "  \"createTime\": \"%s\",\n" +
                        "  \"updateTime\": \"%s\"\n" +
                        "}",
        escapeJson(doc.getId()),
        escapeJson(doc.getTitle()),
        escapeJson(doc.getContent() == null ? "" : doc.getContent()),
        doc.getCreateTime(),
        doc.getUpdateTime()
        );

        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Content-Disposition","attachment;filename=" + URLEncoder.encode(doc.getTitle() + ".md","UTF-8"));
        response.getWriter().write(json);
    }

    private String getUserId(String authHeader){
        String token = authHeader.substring(7);
        return jwtUtil.getUserId(token);
    }

    private String escapeHtml(String s){
        if (s == null) return "";
        return s.replace("&","&amp;")
                .replace("<","&lt;")
                .replace(">","&gt")
                .replace("\"","&quot");
    }

    private String escapeJson(String s){
        if (s == null) return "";
        return s.replace("\\","\\\\")
                .replace("\"","\\\"")
                .replace("\n","\\n")
                .replace("\r","\\r");
    }
}
