///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package MDB;
//
//import Entities.NASARegistrationTable;
//import com.google.android.gcm.server.Sender;
//import java.io.IOException;
//import java.util.List;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import javax.ejb.ActivationConfigProperty;
//import javax.ejb.MessageDriven;
//import javax.jms.JMSException;
//import javax.jms.Message;
//import javax.jms.MessageListener;
//import javax.jms.TextMessage;
//import javax.persistence.EntityManager;
//import javax.persistence.PersistenceContext;
//import javax.persistence.Query;
//import com.google.android.gcm.server.*;
//
///**
// *
// * @author xxc9071
// */
//@MessageDriven(mappedName = "jms/NotifyQueue", activationConfig =
//{
//    @ActivationConfigProperty(propertyName = "acknowledgeMode",
//    propertyValue = "Auto-acknowledge"),
//    @ActivationConfigProperty(propertyName = "destinationType",
//    propertyValue = "javax.jms.Queue")
//})
//public class NotificationMDB implements MessageListener
//{
//
//    @PersistenceContext
//    private EntityManager em;
//
//    @Override
//    public void onMessage(Message message)
//    {
//        try
//        {
//            String issZone = ((TextMessage) message).getText();
//            String jpqlCommand = "SELECT n.regID FROM NASARegistrationTable n WHERE n.zone = '" + issZone + "'";
//            Query query = em.createQuery(jpqlCommand);
//            List<String> regIdList = null;
//            regIdList = query.getResultList();
//
//            System.out.println("regIdList: " + regIdList);
//
//            Sender sender = new Sender("AIzaSyBTPchqyEvcodcuK9YDXlRuDZ4GhwW8ONo");
//            for (String regID : regIdList)
//            {
//
//                com.google.android.gcm.server.Message notifyMessage = new com.google.android.gcm.server.Message.Builder().build();
//                Result result = sender.send(notifyMessage, regID, 5);
//
//
//                // if not null the mobile client has multiple registration IDs and must be
//                // set to one canoncial registration ID
//                if (result.getCanonicalRegistrationId() != null)
//                {
//                    NASARegistrationTable nrt = em.find(NASARegistrationTable.class, regID);
//                    em.remove(nrt);
//
//                    // Methods in MDB are already transactional
//                    nrt = new NASARegistrationTable(result.getCanonicalRegistrationId());
//                    nrt.setZone(Integer.parseInt(issZone));
//                    em.persist(nrt);
//
//                }
//            }
//
//        }
//        catch (IOException ex)
//        {
//            Logger.getLogger(NotificationMDB.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        catch (JMSException ex)
//        {
//            Logger.getLogger(NotificationMDB.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
//
//
//
//    }
//}
