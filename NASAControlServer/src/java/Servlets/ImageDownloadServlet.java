/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Servlets;

import Entities.NASAImageTable;
//import android.util.Base64;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.StringTokenizer;
import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.UserTransaction;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author boferon
 */
public class ImageDownloadServlet extends HttpServlet
{

    @PersistenceContext
    private EntityManager em;
    @Resource
    private UserTransaction transaction;

    /**
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        String location = request.getParameter("location");
        NASAImageTable nit = em.find(NASAImageTable.class, location);

        byte[] image = nit.getImage();
        OutputStream os = response.getOutputStream();
        
        // First four bytes contain length of image comment (encoded in Base64)
        byte[] comment = Base64.encodeBase64URLSafe(nit.getComment().getBytes());
        Integer commentByteLength = comment.length;
        os.write(commentByteLength.byteValue());
        
        // Write comment to stream
        os.write(comment);
        
        System.out.println("before sending before encode length: " + image.length);
        image = Base64.encodeBase64URLSafe(image); // Apache
        System.out.println("before sending after encode length: " + image.length);
        os.write(image); // Write to output stream
        
        os.flush();
        os.close();


    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP
     * <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo()
    {
        return "Short description";
    }// </editor-fold>
}
