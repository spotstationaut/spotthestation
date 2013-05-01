/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Servlets;

import Entities.NASAImageTable;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONObject;

/**
 *
 * @author boferon
 */
public class MarkerDownloadServlet extends HttpServlet
{

    @PersistenceContext
    private EntityManager em;

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
        String jpqlCommand = "SELECT n FROM NASAImageTable n";

        Query query = em.createQuery(jpqlCommand);
        List<NASAImageTable> locations = null;
        locations = query.getResultList();

        StringTokenizer token;
        String jsonString = "{\"Locations\":[";
        String location = "";
        for (NASAImageTable table : locations)
        {
            
            location = table.getLocation();
            token = new StringTokenizer(location, "A");
            jsonString = jsonString + "{\"Lat\":\"" + token.nextToken()
                    + "\",\"Long\":\"" + token.nextToken() + "\"},";
        }
        jsonString = jsonString.substring(0, jsonString.length() - 2); // remove the final comma

        jsonString = jsonString + "}]}";
        
        PrintWriter pw = response.getWriter();
        pw.print(jsonString);
        pw.flush();
        pw.close();

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
