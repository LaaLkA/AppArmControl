import com.fazecast.jSerialComm.SerialPort;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;

public class SerialConnection {
    private final SerialPort serialPort;
    private OutputStream output;

    public SerialConnection(String portName) {
        serialPort = SerialPort.getCommPort(portName);
        serialPort.setBaudRate(9600); // Устанавливаем скорость передачи данных

        if (!serialPort.openPort()) {
            System.err.println("Error: Unable to open port");
            return;
        }

        output = serialPort.getOutputStream();
    }

    public void sendData(String data) {
        try {
            output.write(data.getBytes());
            output.flush(); // Очищаем буфер вывода
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        serialPort.closePort(); // Закрываем порт при завершении
    }
}


class ServoControlApp extends JFrame {
    private static final String SERIAL_PORT = "COM3"; // Установите правильный COM-порт для вашей Arduino

    private final JSlider[] sliders;
    private final JLabel[] labels;
    private final SerialConnection serialConnection;

    public ServoControlApp() {
        super("Управление сервоприводами");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        sliders = new JSlider[4];
        labels = new JLabel[4];
        serialConnection = new SerialConnection(SERIAL_PORT);

        setupUI();
    }

    private void setupUI() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));

        for (int i = 0; i < 4; i++) {
            labels[i] = new JLabel("Серво " + (i + 1));
            panel.add(labels[i]);

            sliders[i] = new JSlider(JSlider.HORIZONTAL, 0, 180, 90);
            sliders[i].setMajorTickSpacing(30);
            sliders[i].setPaintTicks(true);
            sliders[i].setPaintLabels(true);
            sliders[i].addChangeListener(new SliderChangeListener(i));
            panel.add(sliders[i]);
        }

        add(panel);
        pack();
        setLocationRelativeTo(null);
    }

    private class SliderChangeListener implements ChangeListener {
        private final int index;

        public SliderChangeListener(int index) {
            this.index = index;
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            int value = sliders[index].getValue();
            serialConnection.sendData(index + ":" + value + "\n");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ServoControlApp app = new ServoControlApp();
            app.setVisible(true);
        });
    }
}