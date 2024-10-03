import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {
        String filepath = "C:\\Users\\shiro\\OneDrive\\Рабочий стол\\Univer\\vichMath\\trend-line\\src\\data.csv";
        parser parse = new parser(filepath);
        trendLine line = new trendLine(parse.getX(), parse.getY());

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.add(new ChartPanel(parser.getX(), parser.getY(), line));
        frame.setVisible(true);
    }
}

class parser {
    private static List<double[]> dataList;

    public parser(String filepath) throws IOException {
        dataList = new ArrayList<>();
        BufferedReader fileReader = new BufferedReader(new FileReader(filepath));
        String line;
        boolean isFirstLine = true;

        while ((line = fileReader.readLine()) != null) {
            String[] tokens = line.split(";");
            if (isFirstLine) {
                isFirstLine = false;
                continue;
            }

            double[] data = new double[tokens.length - 1];
            for (int j = 1; j < tokens.length; j++) {
                data[j - 1] = Double.parseDouble(tokens[j]);
            }
            dataList.add(data);
        }
    }

    public static double[] getX() { //Среднее значение Hydra01-Hydra05
        double[] x = new double[dataList.size()];

        for (int i = 0; i < dataList.size(); i++) {
            double sum = 0.0;
            int sensNum = dataList.get(i).length - 1;

            for (int j = 0; j < sensNum; j++) {
                sum += dataList.get(i)[j];
            }
            x[i] = sum / sensNum;
        }

        return x;
    }

    public static double[] getY() { //Показания опорного барометра
        double[] y = new double[dataList.size()];

        for (int i = 0; i < dataList.size(); i++) {
            y[i] = dataList.get(i)[dataList.get(i).length - 1];
        }

        return y;
    }
}

class trendLine {
    private double a;
    private double b;

    public trendLine(double[] x, double[] y) {
        coefCalc(x, y);
    }

    private void coefCalc(double[] x, double[] y) {
        int length = x.length;
        double sumX = 0.0;
        double sumY = 0.0;
        double sumXY = 0.0;
        double sumX2 = 0.0;

        for (int i = 0; i < length; i++) {
            sumX += x[i];
            sumY += y[i];
            sumXY += x[i] * y[i];
            sumX2 += x[i] * x[i];
        }

        a = (length * sumXY - sumX * sumY) / (length * sumX2 - sumX * sumX);
        b = (sumY - a * sumX) / length;
    }

    public double getTrendValue(double x) {
        return a * x + b;
    }
}

class ChartPanel extends JPanel {
    private double[] x;
    private double[] y;
    private trendLine line;

    public ChartPanel(double[] x, double[] y, trendLine line) {
        this.x = x;
        this.y = y;
        this.line = line;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setStroke(new BasicStroke(2));

        int width = getWidth();
        int height = getHeight();

        g2d.drawLine(50, height - 50, width - 50, height - 50);
        g2d.drawLine(50, height - 50, 50, 50);

        double xMin = getMin(x);
        double xMax = getMax(x);
        double yMin = getMin(y);
        double yMax = getMax(y);

        for (int i = 0; i < x.length; i++) {
            int scaledX = (int) ((x[i] - xMin) / (xMax - xMin) * (width - 100) + 50);
            int scaledY = (int) ((y[i] - yMin) / (yMax - yMin) * (height - 100));

            g2d.fillOval(scaledX - 3, height - 50 - scaledY - 3, 6, 6);

            String label = String.format("(%.2f, %.2f)", x[i], y[i]);
            g2d.drawString(label, scaledX + 5, height - 50 - scaledY);
        }

        g2d.setColor(Color.RED);
        int x1 = 50;
        int y1 = height - 50 - (int) ((line.getTrendValue(xMin) - yMin) / (yMax - yMin) * (height - 100));
        int x2 = width - 50;
        int y2 = height - 50 - (int) ((line.getTrendValue(xMax) - yMin) / (yMax - yMin) * (height - 100));
        g2d.drawLine(x1, y1, x2, y2);
    }

    private double getMin(double[] array) {
        double min = array[0];
        for (double v : array) {
            if (v < min) {
                min = v;
            }
        }
        return min;
    }

    private double getMax(double[] array) {
        double max = array[0];
        for (double v : array) {
            if (v > max) {
                max = v;
            }
        }
        return max;
    }
}