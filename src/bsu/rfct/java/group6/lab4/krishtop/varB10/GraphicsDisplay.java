package bsu.rfct.java.group6.lab4.krishtop.varB10;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class GraphicsDisplay extends JPanel {
    private Double[][] graphicsData;
    private boolean showAxis = true;
    private boolean showMarkers = true;
    private boolean showDivisions = true;

    private double minX;
    private double maxX;
    private double minY;
    private double maxY;
    private double scale;

    private BasicStroke graphicsStroke;
    private BasicStroke axisStroke;
    private BasicStroke markerStroke;
    private Font axisFont;

    private Rectangle2D.Double selectionRect;
    private int mouseX, mouseY;
    private double originalMinX, originalMaxX, originalMinY, originalMaxY;

    public GraphicsDisplay() {
        setBackground(Color.WHITE);
        graphicsStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f, null, 0.0f);
        axisStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
        markerStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
        axisFont = new Font("Serif", Font.BOLD, 36);
    }

    public void showGraphics(Double[][] graphicsData) {
        this.graphicsData = graphicsData;

        if (this.graphicsData != null && this.graphicsData.length > 0 && originalMinX == 0) {
            originalMinX = this.graphicsData[0][0];
            originalMaxX = this.graphicsData[this.graphicsData.length - 1][0];
            originalMinY = this.graphicsData[0][1];
            originalMaxY = originalMinY;

            for (int i = 1; i < this.graphicsData.length; i++) {
                if (this.graphicsData[i][1] < originalMinY) {
                    originalMinY = this.graphicsData[i][1];
                }
                if (this.graphicsData[i][1] > originalMaxY) {
                    originalMaxY = this.graphicsData[i][1];
                }
            }
        }

        if (this.graphicsData != null && this.graphicsData.length > 0) {
            minX = originalMinX;
            maxX = originalMaxX;
            minY = originalMinY;
            maxY = originalMaxY;
        }
        rescale();
        repaint();
    }

    private void rescale() {
        double scaleX = getSize().getWidth() / (maxX - minX);
        double scaleY = getSize().getHeight() / (maxY - minY);
        scale = Math.min(scaleX, scaleY);

        if (scale == scaleX) {
            double yIncrement = (getSize().getHeight() / scale - (maxY - minY)) / 2;
            maxY += yIncrement;
            minY -= yIncrement;
        }
        if (scale == scaleY) {
            double xIncrement = (getSize().getWidth() / scale - (maxX - minX)) / 2;
            maxX += xIncrement;
            minX -= xIncrement;
        }
    }

    public void setShowAxis(boolean showAxis) {
        this.showAxis = showAxis;
        repaint();
    }

    public void setShowMarkers(boolean showMarkers) {
        this.showMarkers = showMarkers;
        repaint();
    }
    public void setShowDivisions(boolean showDivisions) {
        this.showDivisions = showDivisions;
        repaint();
    }




    public void setSelectionRect(Rectangle2D.Double rect) {
        this.selectionRect = rect;
        repaint();
    }

    public void zoom(Rectangle2D.Double selectionRect) {
        Point2D.Double startPoint = new Point2D.Double(selectionRect.x, selectionRect.y);
        Point2D.Double endPoint = new Point2D.Double(selectionRect.x + selectionRect.width, selectionRect.y + selectionRect.height);

        Point2D.Double startXY = pointToXY(startPoint);
        Point2D.Double endXY = pointToXY(endPoint);

        minX = startXY.x;
        maxX = endXY.x;
        minY = endXY.y;
        maxY = startXY.y;
        rescale(); // Пересчитываем масштаб после изменения границ
        repaint();
    }

    protected Point2D.Double pointToXY(Point2D.Double point) {
        double x = minX + point.x / scale;
        double y = maxY - point.y / scale;
        return new Point2D.Double(x, y);

    }



    public void resetZoom() {
        minX = originalMinX;
        maxX = originalMaxX;
        minY = originalMinY;
        maxY = originalMaxY;
        rescale(); // Пересчитываем масштаб после сброса
        repaint();
    }

    public void setMouseCoordinates(int x, int y) {
        this.mouseX = x;
        this.mouseY = y;
        repaint();
    }


    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (graphicsData == null || graphicsData.length == 0) {
            return;
        }

        Graphics2D canvas = (Graphics2D) g;
        Stroke oldStroke = canvas.getStroke();
        Color oldColor = canvas.getColor();
        Paint oldPaint = canvas.getPaint();
        Font oldFont = canvas.getFont();


        if (showAxis) {
            paintAxis(canvas);
        }
        paintGraphics(canvas);

        if (showMarkers) {
            paintMarkers(canvas);
        }

        if (showDivisions) {
            paintDivisions(canvas);
        }

        if (selectionRect != null) {
            canvas.setColor(Color.BLACK);
            Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0);
            canvas.setStroke(dashed);
            canvas.draw(selectionRect);
        }

        //selectionRect = null;



        if (graphicsData != null) {
            for (int i = 0; i < graphicsData.length; i++) {
                Point2D.Double point = xyToPoint(graphicsData[i][0], graphicsData[i][1]);
                if (mouseX > point.x - 10 && mouseX < point.x + 10 &&
                        mouseY > point.y - 10 && mouseY < point.y + 10) {
                    String label = String.format("(%.2f, %.2f)", graphicsData[i][0], graphicsData[i][1]);
                    canvas.drawString(label, (float) point.x + 5, (float) point.y - 5);
                    break;
                }
            }
        }

        canvas.setFont(oldFont);
        canvas.setPaint(oldPaint);
        canvas.setColor(oldColor);
        canvas.setStroke(oldStroke);
    }

//    protected void paintDivisions(Graphics2D canvas) {
//        canvas.setStroke(new BasicStroke(1.0f));
//        canvas.setColor(Color.LIGHT_GRAY);
//
//        FontRenderContext context = canvas.getFontRenderContext(); // Не забудьте про context
//
//        double divisionStepX = (maxX - minX) / 100;  // Шаг 1/100 по X
//        double startX = minX + (divisionStepX - minX % divisionStepX) % divisionStepX; // Округляем до ближайшего деления
//
//        double x = minX;
//
//        for (int i = 0; x <= maxX; i++) {
//             x = startX + i * divisionStepX;
//            Point2D.Double p1 = xyToPoint(x, maxY);
//            Point2D.Double p2 = xyToPoint(x, minY);
//
//            if (i % 5 == 0) { // Каждое пятое деление длиннее
//                canvas.setStroke(new BasicStroke(2.0f)); // Утолщаем линию
//                canvas.setColor(Color.GRAY); // Меняем цвет
//                canvas.draw(new Line2D.Double(shiftPoint(p1,0,-10), shiftPoint(p2,0,10)));
//                canvas.setStroke(new BasicStroke(1.0f));// Возвращаем тонкую линию
//                canvas.setColor(Color.LIGHT_GRAY);// Возвращаем цвет
//                // Подписи делений по оси X (только для каждого пятого)
//                String label = String.format("%.2f", x);
//                Rectangle2D bounds = axisFont.getStringBounds(label, context);
//                canvas.drawString(label, (float)(p1.getX() - bounds.getWidth()/2), (float)(p2.getY() + bounds.getHeight() + 5));
//
//
//            } else {
//                canvas.draw(new Line2D.Double(shiftPoint(p1,0,-5), shiftPoint(p2,0,5)));
//            }
//        }
//
//
//
//
//        double divisionStepY = (maxY - minY) / 100;  // Шаг 1/100 по Y
//        double startY = minY + (divisionStepY - minY % divisionStepY) % divisionStepY; // Округляем до ближайшего деления
//
//        double y = minY;
//        for (int i = 0; y <= maxY; i++) {
//            y = startY + i * divisionStepY;
//
//            Point2D.Double p1 = xyToPoint(minX, y);
//            Point2D.Double p2 = xyToPoint(maxX, y);
//
//            if (i % 5 == 0) { // Каждое пятое деление длиннее
//
//                canvas.setStroke(new BasicStroke(2.0f));
//                canvas.setColor(Color.GRAY);
//                canvas.draw(new Line2D.Double(shiftPoint(p1,-10,0), shiftPoint(p2,10,0)));
//                canvas.setStroke(new BasicStroke(1.0f));
//                canvas.setColor(Color.LIGHT_GRAY);
//                String label = String.format("%.2f", y);
//                Rectangle2D bounds = axisFont.getStringBounds(label, context);
//
//                canvas.drawString(label, (float)(p1.getX() - bounds.getWidth() - 15), (float)(p1.getY() + bounds.getHeight()/4));
//
//            } else {
//                canvas.draw(new Line2D.Double(shiftPoint(p1,-5,0), shiftPoint(p2,5,0)));
//            }
//        }
//    }

    protected void paintDivisions(Graphics2D canvas) {
        canvas.setStroke(new BasicStroke(1.0f));
        canvas.setColor(Color.LIGHT_GRAY);
        Font smallerFont = axisFont.deriveFont(12f);

        double divisionStepX = (maxX - minX) / 100;
        double startX = minX; // Округляем до ближайшего деления

        FontRenderContext context = canvas.getFontRenderContext();
        if (minY <= 0 && maxY >= 0) { // Рисуем деления на оси X только если она видна
            double x = 0;
            for (int i = 0; x <= maxX; i++) {
                x = startX + i * divisionStepX;

                Point2D.Double p1 = xyToPoint(x, 0); // y = 0 для оси X
                Point2D.Double p2;


                if (i % 5 == 0) {
                    canvas.setStroke(new BasicStroke(2.0f));
                    canvas.setColor(Color.GRAY);
                    p2 = shiftPoint(p1,0,10);

                    canvas.draw(new Line2D.Double(p1, p2));
                    canvas.setStroke(new BasicStroke(1.0f));
                    canvas.setColor(Color.LIGHT_GRAY);


                    String label = String.format("%.2f", x);
                    canvas.setFont(smallerFont); // Устанавливаем smallerFont перед вычислением bounds и отрисовкой текста
                    Rectangle2D bounds = smallerFont.getStringBounds(label, context);
                    canvas.drawString(label, (float) (p1.getX() - bounds.getWidth() / 2), (float) (p2.getY() + bounds.getHeight()));
                    canvas.setFont(axisFont);

                } else {
                    p2 = shiftPoint(p1,0,5);

                    canvas.draw(new Line2D.Double(p1, p2));

                }

            }
        }

        double divisionStepY = (maxY - minY) / 100;
        double startY = minY; // Округляем до ближайшего деления

        context = canvas.getFontRenderContext();
        if (minX <= 0 && maxX >= 0) { // Рисуем деления на оси Y только если она видна
            double y = 0;
            for (int i = 0; y <= maxY; i++) {
                 y = startY + i * divisionStepY;


                Point2D.Double p1 = xyToPoint(0, y); // x = 0 для оси Y
                Point2D.Double p2;
                if (i % 5 == 0) {
                    canvas.setStroke(new BasicStroke(2.0f));
                    canvas.setColor(Color.GRAY);
                    p2 = shiftPoint(p1,10,0);
                    canvas.draw(new Line2D.Double(p1, p2));
                    canvas.setStroke(new BasicStroke(1.0f));
                    canvas.setColor(Color.LIGHT_GRAY);
                    String label = String.format("%.2f", y);
                    canvas.setFont(smallerFont);// Устанавливаем smallerFont перед вычислением bounds и отрисовкой текста
                    Rectangle2D bounds = smallerFont.getStringBounds(label, context);
                    canvas.drawString(label, (float) (p2.getX() - bounds.getWidth() - 10), (float) (p1.getY() + bounds.getHeight() / 4));
                    canvas.setFont(axisFont);
                } else {
                    p2 = shiftPoint(p1,5,0);
                    canvas.draw(new Line2D.Double(p1, p2));


                }
            }
        }
    }

    protected double niceNumber(double v, boolean round) {
        int exponent = (int)Math.floor(Math.log10(v));
        double fraction = v / Math.pow(10,exponent);
        double niceFraction;

        if (round) {
            if (fraction < 1.5)
                niceFraction = 1;
            else if (fraction < 3)
                niceFraction = 2;
            else if (fraction < 7)
                niceFraction = 5;
            else
                niceFraction = 10;

        } else {

            if (fraction <= 1)
                niceFraction = 1;
            else if (fraction <= 2)
                niceFraction = 2;
            else if (fraction <= 5)
                niceFraction = 5;
            else
                niceFraction = 10;
        }
        return niceFraction * Math.pow(10, exponent);
    }

    protected void paintGraphics(Graphics2D canvas) {
        float[] dashPattern = {40f, 10f, 10f, 10f, 20f, 10f, 10f, 10f};
        float dashPhase = 0f; // Начальная фаза пунктира
        BasicStroke dashedStroke = new BasicStroke(
                5.0f, // Толщина линии
                BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER,
                10.0f,
                dashPattern,
                dashPhase
        );

        canvas.setStroke(dashedStroke);
        canvas.setColor(Color.RED);

        GeneralPath graphics = new GeneralPath();
        for (int i = 0; i < graphicsData.length; i++) {
            Point2D.Double point = xyToPoint(graphicsData[i][0], graphicsData[i][1]);
            if (i > 0) {
                graphics.lineTo(point.getX(), point.getY());
            } else {
                graphics.moveTo(point.getX(), point.getY());
            }
        }
        canvas.draw(graphics);
    }

    protected void paintMarkers(Graphics2D canvas) {
        canvas.setStroke(markerStroke);
        canvas.setColor(Color.RED);
        canvas.setPaint(Color.RED);

        for (Double[] point : graphicsData) {
            double y = point[1];
            double closestInt = Math.round(y);
            if (Math.abs(y - closestInt) <= 0.1) {
                canvas.setColor(Color.BLUE);
            } else {
                canvas.setColor(Color.RED);
            }

            Point2D.Double center = xyToPoint(point[0], point[1]);

            Line2D.Double line1 = new Line2D.Double(center.x - 10, center.y - 10, center.x + 10, center.y + 10);
            Line2D.Double line2 = new Line2D.Double(center.x - 10, center.y + 10, center.x + 10, center.y - 10);
            Line2D.Double line3 = new Line2D.Double(center.x - 10, center.y + 10, center.x + 10, center.y + 10);
            Line2D.Double line4 = new Line2D.Double(center.x - 10, center.y - 10, center.x + 10, center.y - 10);
            Line2D.Double line5 = new Line2D.Double(center.x - 10, center.y - 10, center.x - 10, center.y + 10);
            Line2D.Double line6 = new Line2D.Double(center.x + 10, center.y - 10, center.x + 10, center.y + 10);


            canvas.draw(line1);
            canvas.draw(line2);
            canvas.draw(line3);
            canvas.draw(line4);
            canvas.draw(line5);
            canvas.draw(line6);

        }
    }



    protected void paintAxis(Graphics2D canvas) {
        canvas.setStroke(axisStroke);
        canvas.setColor(Color.BLACK);
        canvas.setPaint(Color.BLACK);
        canvas.setFont(axisFont);

        FontRenderContext context = canvas.getFontRenderContext();

        if (minX <= 0.0 && maxX >= 0.0) {
            canvas.draw(new Line2D.Double(xyToPoint(0, maxY), xyToPoint(0, minY)));

            GeneralPath arrow = new GeneralPath();
            Point2D.Double lineEnd = xyToPoint(0, maxY);
            arrow.moveTo(lineEnd.getX(), lineEnd.getY());
            arrow.lineTo(arrow.getCurrentPoint().getX() + 5, arrow.getCurrentPoint().getY() + 20);
            arrow.lineTo(arrow.getCurrentPoint().getX() - 10, arrow.getCurrentPoint().getY());
            arrow.closePath();
            canvas.draw(arrow);
            canvas.fill(arrow);

            Rectangle2D bounds = axisFont.getStringBounds("y", context);
            Point2D.Double labelPos = xyToPoint(0, maxY);
            canvas.drawString("y", (float) labelPos.getX() + 10, (float) (labelPos.getY() - bounds.getY()));
        }

        if (minY <= 0.0 && maxY >= 0.0) {
            canvas.draw(new Line2D.Double(xyToPoint(minX, 0), xyToPoint(maxX, 0)));

            GeneralPath arrow = new GeneralPath();
            Point2D.Double lineEnd = xyToPoint(maxX, 0);
            arrow.moveTo(lineEnd.getX(), lineEnd.getY());
            arrow.lineTo(arrow.getCurrentPoint().getX() - 20, arrow.getCurrentPoint().getY() - 5);
            arrow.lineTo(arrow.getCurrentPoint().getX(), arrow.getCurrentPoint().getY() + 10);
            arrow.closePath();
            canvas.draw(arrow);
            canvas.fill(arrow);

            Rectangle2D bounds = axisFont.getStringBounds("x", context);
            Point2D.Double labelPos = xyToPoint(maxX, 0);
            canvas.drawString("x", (float) (labelPos.getX() - bounds.getWidth() - 10), (float) (labelPos.getY() + bounds.getY()));
        }
    }


    protected Point2D.Double xyToPoint(double x, double y) {
        double deltaX = x - minX;
        double deltaY = maxY - y;
        return new Point2D.Double(deltaX * scale, deltaY * scale);
    }

    protected Point2D.Double shiftPoint(Point2D.Double src, double deltaX, double deltaY) {
        Point2D.Double dest = new Point2D.Double();
        dest.setLocation(src.getX() + deltaX, src.getY() + deltaY);
        return dest;
    }
}
