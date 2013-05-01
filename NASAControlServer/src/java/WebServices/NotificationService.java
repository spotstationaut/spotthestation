/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package WebServices;

import Entities.NASARegistrationTable;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ws.rs.PathParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.enterprise.context.RequestScoped;
import javax.persistence.PersistenceContext;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

/**
 * REST Web Service
 *
 * @author xxc9071
 */
@Path("notification")
@RequestScoped
public class NotificationService
{

//    @Resource(mappedName = "jms/ConnectionFactory")
//    private ConnectionFactory connectionFactory;
//    @Resource(mappedName = "jms/NotifyQueue")
//    private Queue queue;
//    private Connection conn;
//    private Session session;
//    private MessageProducer producer;
    private boolean isInitialized;
    @PersistenceContext
    private EntityManager em;
    @Resource
    private UserTransaction transaction;

    /**
     * Creates a new instance of NotificationService
     */
    public NotificationService()
    {
        isInitialized = false;

    }

    @GET
    @Consumes("text/plain")
    @Path("notify/{issZone}")
    public String startNotificationProcess(@PathParam("issZone") String issZone)
    {
        String jpqlCommand = "SELECT n.regID FROM NASARegistrationTable n WHERE n.zone = '" + issZone + "'";
        System.out.println("jpqlCommand: " + jpqlCommand);
        Query query = em.createQuery(jpqlCommand);
        List<String> regIdList = null;
        regIdList = query.getResultList();
        System.out.println("i'm in restful List size: " + regIdList.size());
        Sender sender = new Sender("AIzaSyBTPchqyEvcodcuK9YDXlRuDZ4GhwW8ONo");
        for (String regID : regIdList)
        {
            try
            {
                com.google.android.gcm.server.Message notifyMessage = new com.google.android.gcm.server.Message.Builder().build();
                Result result = sender.send(notifyMessage, regID, 5);

                System.out.println("int for loop regID: " + regID);
                // if not null the mobile client has multiple registration IDs and must be
                // set to one canoncial registration ID
                System.out.println("result.getCanonicalRegistrationId(): " + result.getCanonicalRegistrationId());
                if (result.getCanonicalRegistrationId() != null)
                {
                    NASARegistrationTable nrt = em.find(NASARegistrationTable.class, regID);
                    transaction.begin();
                    em.remove(nrt);
                    transaction.commit();
                    // Methods in MDB are already transactional
                    nrt = new NASARegistrationTable(result.getCanonicalRegistrationId());
                    nrt.setZone(Integer.parseInt(issZone));
                    transaction.begin();
                    em.persist(nrt);
                    transaction.commit();
                }
            }
            catch (RollbackException ex)
            {
                Logger.getLogger(NotificationService.class.getName()).log(Level.SEVERE, null, ex);
            }
            catch (HeuristicMixedException ex)
            {
                Logger.getLogger(NotificationService.class.getName()).log(Level.SEVERE, null, ex);
            }
            catch (HeuristicRollbackException ex)
            {
                Logger.getLogger(NotificationService.class.getName()).log(Level.SEVERE, null, ex);
            }
            catch (SecurityException ex)
            {
                Logger.getLogger(NotificationService.class.getName()).log(Level.SEVERE, null, ex);
            }
            catch (IllegalStateException ex)
            {
                Logger.getLogger(NotificationService.class.getName()).log(Level.SEVERE, null, ex);
            }
            catch (NotSupportedException ex)
            {
                Logger.getLogger(NotificationService.class.getName()).log(Level.SEVERE, null, ex);
            }
            catch (SystemException ex)
            {
                Logger.getLogger(NotificationService.class.getName()).log(Level.SEVERE, null, ex);
            }
            catch (IOException ex)
            {
                Logger.getLogger(NotificationService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
//        String isSuccessful = "false";
//
//        if (!isInitialized)
//        {
//            try
//            {
//                System.out.println("connectionFactory: " + connectionFactory);
//                conn = connectionFactory.createConnection();
//                session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
//                producer = session.createProducer(queue);
//            }
//            catch (JMSException ex)
//            {
//                Logger.getLogger(NotificationService.class.getName()).log(Level.SEVERE, null, ex);
//            }
//
//            isInitialized = true;
//        }
//
//
//        try
//        {
//            TextMessage msg = session.createTextMessage(iisZone);
//            producer.send(msg);
//            isSuccessful = "true";
//        }
//        catch (JMSException ex)
//        {
//            Logger.getLogger(NotificationService.class.getName()).log(Level.SEVERE, null, ex);
//        }
//

        return "true";
    }

    /**
     * Retrieves representation of an instance of
     * WebServices.NotificationService
     *
     * @return an instance of java.lang.String
     */
    @GET
    @Produces("application/xml")
    public String getXml()
    {
        //TODO return proper representation object
        throw new UnsupportedOperationException();
    }

    /**
     * PUT method for updating or creating an instance of NotificationService
     *
     * @param content representation for the resource
     * @return an HTTP response with content of the updated or created resource.
     */
    @PUT
    @Consumes("application/xml")
    public void putXml(String content)
    {
    }
}
