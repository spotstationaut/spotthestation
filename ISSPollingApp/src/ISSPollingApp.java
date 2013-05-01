
import Threads.HttpPollingThread;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author xxc9071
 */
public class ISSPollingApp extends JPanel implements ActionListener
{

    private static final int POLLING_PERIOD = 7200000; // 7200000ms = 2 hours
    private JLabel statusLabel;
    private JButton startButton, stopButton;
    private Timer timer;

    public ISSPollingApp()
    {
        super();
        statusLabel = new JLabel();
        startButton = new JButton("Start");
        stopButton = new JButton("Stop");
        HttpPollingThread hpt = new HttpPollingThread(statusLabel);
        timer = new Timer(POLLING_PERIOD, hpt);

        // Not started by default
        stopButton.setEnabled(false);
        
        add(startButton);
        add(stopButton);
        add(statusLabel);
        startButton.addActionListener(this);
        stopButton.addActionListener(this);
        setPreferredSize(new Dimension(400, 100));
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        Object source = e.getSource();
        if (source == startButton)
        {
            timer.start();

            // Don't allow to be started multiple times
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
        }
        else if (source == stopButton)
        {
            timer.stop();

            startButton.setEnabled(true);
            stopButton.setEnabled(false);
        }
    }

    public static void main(String[] args)
    {
        JFrame frame = new JFrame("Shape Sketcher");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new ISSPollingApp());
        frame.pack();
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension screenDimension = tk.getScreenSize();
        Dimension frameDimension = frame.getSize();
        frame.setLocation((screenDimension.width - frameDimension.width) / 2,
                (screenDimension.height - frameDimension.height) / 2);
        frame.setVisible(true);
    }
}
