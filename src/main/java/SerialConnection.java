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
    private final JSlider[] sliders;
    private final JLabel[] labels;
    private SerialConnection serialConnection;
    private final JComboBox<String> portComboBox; // Добавляем JComboBox для выбора COM-порта

    public ServoControlApp() {
        super("Управление сервоприводами");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10)); // Увеличиваем количество строк для добавления JComboBox

        // Добавляем метку для COM-порта
        JLabel portLabel = new JLabel("COM-порт:");
        panel.add(portLabel);

        // Создаем JComboBox для выбора COM-порта
        portComboBox = new JComboBox<>();
        SerialPort[] ports = SerialPort.getCommPorts();
        for (SerialPort port : ports) {
            portComboBox.addItem(port.getSystemPortName());
        }
        panel.add(portComboBox);

        sliders = new JSlider[4];
        labels = new JLabel[4];

        // Используем выбранный COM-порт из JComboBox
        serialConnection = new SerialConnection((String) portComboBox.getSelectedItem());

        setupUI(panel); // Передаем панель в метод setupUI

        // Добавляем слушатель изменений для JComboBox
        portComboBox.addActionListener(e -> {
            serialConnection.close(); // Закрываем текущее соединение
            serialConnection = new SerialConnection((String) portComboBox.getSelectedItem()); // Создаем новое соединение с выбранным портом
        });
    }

    private void setupUI(JPanel panel) { // Добавляем параметр panel
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