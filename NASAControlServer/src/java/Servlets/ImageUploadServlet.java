/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Servlets;

import Entities.NASAImageTable;
//import android.util.Base64;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author xxc9071
 */
public class ImageUploadServlet extends HttpServlet
{

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
    @PersistenceContext
    private EntityManager em;
    @Resource
    private UserTransaction transaction;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
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
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        // TODO Auto-generated method stub
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        System.out.println("GETTING HERE?????????????????????????????????????");
        try
        {
            InputStream in = request.getInputStream();
            BufferedReader r = new BufferedReader(new InputStreamReader(in));
            StringBuffer buf = new StringBuffer();
            String line;
            System.out.println("-2");
            //Read the BufferedReader out and receives String data
            while ((line = r.readLine()) != null)
            {
                buf.append(line);
            }


            String imageString = buf.toString();
            //System.out.println("IMAGE STRING = " + imageString);
            System.out.println("-1");
            String infoString = imageString.substring(0, imageString.indexOf("="));
            byte[] infoBytes = Base64.decodeBase64(infoString);
            infoString = new String(infoBytes);
            System.out.println("0");
            System.out.println("imageString: " + infoString);
            StringTokenizer token = new StringTokenizer(infoString, ";");
            double latitude = Double.parseDouble(token.nextToken());
            double longitude = Double.parseDouble(token.nextToken());
            int imageLength = Integer.parseInt(token.nextToken());
            System.out.println("1");
            String comment = token.nextToken();

            String imageData = imageString.substring(imageString.indexOf("=") + 1);
            System.out.println("2");
            byte[] imageByteArray = Base64.decodeBase64(imageData); // Uses apache
            byte[] finalByteArrayImage = new byte[imageLength];
            System.arraycopy(imageByteArray, 0, finalByteArrayImage, 0, imageLength);
            System.out.println("3");
            System.out.println("location: " + latitude + " " + longitude + "  omg, i'm missing");
            System.out.println("comment: " + comment);
            NASAImageTable nit = new NASAImageTable("" + latitude + "A" + longitude);
            nit.setImage(finalByteArrayImage);
            nit.setComment(comment);

            transaction.begin();
            em.persist(nit);
            transaction.commit();
        }
        catch (RollbackException ex)
        {
            Logger.getLogger(ImageUploadServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (HeuristicMixedException ex)
        {
            Logger.getLogger(ImageUploadServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (HeuristicRollbackException ex)
        {
            Logger.getLogger(ImageUploadServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (SecurityException ex)
        {
            Logger.getLogger(ImageUploadServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (IllegalStateException ex)
        {
            Logger.getLogger(ImageUploadServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (NotSupportedException ex)
        {
            Logger.getLogger(ImageUploadServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (SystemException ex)
        {
            Logger.getLogger(ImageUploadServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
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
