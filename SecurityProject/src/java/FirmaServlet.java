/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import static java.lang.System.out;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.security.KeyPair;
import javax.xml.bind.DatatypeConverter;
import security.FileEncryption;


/**
 *
 * @author Noe
 */
@WebServlet(urlPatterns = {"/FirmaServlet"})
public class FirmaServlet extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet FirmaServlet</title>");            
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet FirmaServlet at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //processRequest(request, response);
        
        try (PrintWriter out = response.getWriter()) {
            Cookie[] cookies = request.getCookies();
            ServletContext sc = getServletContext();
            if(cookies != null){
                for(Cookie cookie : cookies){
                    if(cookie.getName().equals("JSESSIONID")) {
                        User current = (User) sc.getAttribute("currentUser");
                        if (current == null || current.getSessionId() == null || !current.getSessionId().getId().equals(cookie.getValue())) {
                            response.sendRedirect("unauthorized-error.html");
                            return;
                        }
                    }
                }
            }        
        //}
        
        //Parametros 
        String fileName = request.getParameter("fileName");
        String destinoName = request.getParameter("destinoName")+ "/" + fileName + ".pdf";
        String textoClave = request.getParameter("textoClave");
        
        response.setContentType("text/html;charset=UTF-8"); 
        
        //Instancio EncryptFirma
        EncryptFirma encrypt = new EncryptFirma();
        
        //Firmo digitalmente el archivo
        //try{
            if(!encrypt.existenClaves()){
                //Si no existe creo las claves en /tmp
                KeyPair key = encrypt.generateKey();
                encrypt.guardoClaves(key);
            }
            
            //Cargo las claves (publica y privada)
            KeyPair key = encrypt.CargoClave();
            
            //Codificacion Base64Binary de clave privada
            final String priv = DatatypeConverter.printBase64Binary( key.getPrivate().getEncoded() ); 
            
            //Realizo la firma para cifrar
            final String firma = encrypt.firmar(textoClave, priv);
            
            //Agrego la firma en un documento
            Document documento = new Document();

            PdfWriter.getInstance(documento, new FileOutputStream(destinoName));
            documento.open();

            Paragraph parrafo = new Paragraph(firma);
            documento.add(parrafo);
            documento.close();
            
            //Como el archivo fue firmado correctamente despliego mensaje
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet MyServlet</title>");            
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Archivo firmado correctamente</h1>");
            out.println("<h3>Archivo: " + destinoName + "</h3>");
            out.println("<h3><a href=\"firmadoDigital.html\">Volver</a></h3>");
            out.println("</body>");
            out.println("</html>");
        }
        catch(Exception ex){
            response.sendRedirect("exception-error.html");   
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
