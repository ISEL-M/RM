import javax.swing.*;
import javax.swing.text.DefaultFormatter;
import java.awt.*;


public class GUI extends JFrame {
    private JPanel mainPanel;
    private JButton btnStop, btnFront, btnBack, btnRight, btnLeft;
    private JRadioButton onOffButton;
    private JFormattedTextField radius, angle, distance;
    private JTextField robotName;
    private JTextArea console;
    private JCheckBox debugCheckBox;
    private JButton btnStartTrajectory;
    private JFormattedTextField trajF , trajX, trajY;
    private JButton btnStep;
    private JButton btnWall;
    private JFormattedTextField speed;
    private final Variables v;

    private void updateConsole(String msg){
        if (v.isDebug()){
            String new_msg = console.getText() + "\n" + msg ;
            console.setText(new_msg);
        }
    }

    public GUI() {
        //set distatance radius and angle to be numeric
        JFormattedTextField.AbstractFormatterFactory formatter = new JFormattedTextField.AbstractFormatterFactory() {
            @Override
            public JFormattedTextField.AbstractFormatter getFormatter(JFormattedTextField tf) {
                DefaultFormatter formatter = new DefaultFormatter();
                formatter.setValueClass(Integer.class);
                formatter.setAllowsInvalid(false);
                return formatter;
            }
        };
        distance.setFormatterFactory(formatter);
        radius.setFormatterFactory(formatter);
        angle.setFormatterFactory(formatter);
        speed.setFormatterFactory(formatter);

        formatter = new JFormattedTextField.AbstractFormatterFactory() {
            @Override
            public JFormattedTextField.AbstractFormatter getFormatter(JFormattedTextField tf) {
                DefaultFormatter formatter = new DefaultFormatter();
                formatter.setValueClass(Double.class);
                formatter.setAllowsInvalid(false);
                return formatter;
            }
        };
        trajX.setFormatterFactory(formatter);
        trajY.setFormatterFactory(formatter);
        trajF.setFormatterFactory(formatter);
        setContentPane(mainPanel);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("Robotica Movel");
        setVisible(true);
        setMinimumSize(new Dimension(850, 300));

        v = new Variables();

        robotName.addActionListener(e -> {
            v.setRobotName(robotName.getText());
            updateConsole("New Speed: " + v.getRobotName());
        });

        speed.addActionListener(e -> {
            v.setSpeed(Integer.parseInt(speed.getText()));
            updateConsole("New Robot Name: " + v.getSpeed());
        });

        radius.addActionListener(e -> {
            v.setRadius(Integer.parseInt(radius.getText()));
            updateConsole("New Radius: " + v.getRadius());
        });

        angle.addActionListener(e -> {
            v.setAngle(Integer.parseInt(angle.getText()));
            updateConsole("New Angle: " + v.getAngle());
        });

        distance.addActionListener(e -> {
            v.setDistance(Integer.parseInt(distance.getText()));
            updateConsole("New Distance: " + v.getDistance());
        });

        //get and set Trajectory
        trajX.addActionListener(e -> {
            v.getTrajectory().setXf(Double.parseDouble(trajX.getText()));
            updateConsole("Xf trajectory: " + v.getTrajectory().getXf());
        });

        trajY.addActionListener(e -> {
            v.getTrajectory().setYf(Double.parseDouble(trajY.getText()));
            updateConsole("Yf trajectory: " + v.getTrajectory().getYf());
        });

        trajF.addActionListener(e -> {
            v.getTrajectory().setF(Double.parseDouble(trajF.getText()));
            updateConsole("f trajectory: " + v.getTrajectory().getF());
        });

        //get and set Trajectory



        //OnOff button
        onOffButton.addActionListener(e -> {
            if (v.isDebug())
                updateConsole("" + onOffButton.isSelected());

            v.setOnOff(onOffButton.isSelected());
            if (v.isOnOff()) {
                boolean ok = v.getRobot().OpenEV3(v.getRobotName());
                if (!ok) {
                    //onOffButton.setBackground(Color.RED);
                    onOffButton.setSelected(false);
                    v.setOnOff(false);
                }
            } else {
                v.getRobot().CloseEV3();
            }
        });

        //Debug button
        debugCheckBox.addActionListener(e -> {
            v.getRobot().Debug();
            v.setDebug(debugCheckBox.isSelected());
            console.setEditable(v.isDebug());
        });

        //Buttons
        btnStop.addActionListener(e -> {
            updateConsole("Stop Button");
            if (v.isOnOff()) {
                v.getRobot().Parar(true);
            }
        });
        btnFront.addActionListener(e -> {
            updateConsole("Move Front");
            if (v.isOnOff()) {
                v.getRobot().setWork("Reta", v.getDistance());
                v.getRobot().setWork("Stop");
            }
        });
        btnBack.addActionListener(e -> {
            updateConsole("Move Back");
            if (v.isOnOff()) {
                v.getRobot().setWork("Back", v.getDistance());
                v.getRobot().setWork("Stop");
            }
        });
        btnLeft.addActionListener(e -> {
            updateConsole("Move Left");
            if (v.isOnOff()) {
                v.getRobot().setWork("Left", v.getRadius(), v.getAngle());
                v.getRobot().setWork("Stop");
            }
        });
        btnRight.addActionListener(e -> {
            updateConsole("Move Right");
            if (v.isOnOff()) {
                v.getRobot().setWork("Right", v.getRadius(), v.getAngle());
                v.getRobot().setWork("Stop");
            }
        });
        btnStartTrajectory.addActionListener(e -> {
            updateConsole("Start Trajectory: " + v.getTrajectory());
            if (v.isOnOff())
                v.getRobot().trajectories(v.getTrajectory());
        });
        btnWall.addActionListener(e -> {
            updateConsole("Following wall");
            if (v.isOnOff())
                v.getRobot().Wall();
        });

        btnStep.addActionListener(e->{
            if ( v.isDebug())
                v.getRobot().Step();
        });


        robotName.setText(v.getRobotName());
        radius.setText("" + v.getRadius());
        angle.setText("" + v.getAngle());
        distance.setText("" + v.getDistance());
        speed.setText("" + v.getSpeed());
        onOffButton.setSelected(v.isOnOff());
        debugCheckBox.setSelected(v.isDebug());
        debugCheckBox.setSelected(v.isDebug());
        trajX.setText("" + v.getTrajectory().getXf());
        trajY.setText("" + v.getTrajectory().getYf());
        trajF.setText("" + v.getTrajectory().getF());


    }

    public static void main(String[] args) {
        GUI gui = new GUI();
        System.out.println();
    }
}
