package rfe.bsu.SikolonkoMa.laba4;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class GraphicsDisplay extends JPanel {
    private Double[][] graphicsData;
    private ArrayList<Double[]> originalData;
    private boolean showAxis = true;
    private boolean firstlunc = true;
    private boolean showMarkers = true;
    private boolean showArea = false;
    private boolean Rotate = false;
    private double minX;
    private double maxX;
    private double minY;
    private double maxY;
    private int selectedMarker;
    private double[][] viewport;
    private ArrayList<double[][]> undoHistory;
    private double scale;
    private BasicStroke graphicsStroke;
    private BasicStroke axisStroke;
    private BasicStroke markerStroke;
    private BasicStroke selectionStroke;
    private Font labelsFont;
    private static DecimalFormat formatter;
    private boolean scaleMode;
    private boolean changeMode;
    private double[] originalPoint;
    private Rectangle2D.Double selectionRect;
    private Font axisFont;
    private Font areaFont;

    static {
        GraphicsDisplay.formatter = (DecimalFormat)NumberFormat.getInstance();
    }
    public GraphicsDisplay() {

        selectedMarker = -1;
        viewport = new double[2][2];
        undoHistory = new ArrayList<double[][]>(5);
        scaleMode = false;
        changeMode = false;
        originalPoint = new double[2];
        selectionRect = new Rectangle2D.Double();
        setBackground(Color.WHITE);
        graphicsStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_ROUND, 10.0f, null, 0.0f);
        axisStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
        markerStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
        selectionStroke = new BasicStroke(1.0f, 0, 0, 10.0f,
                new float[] { 10.0f, 10.0f }, 0.0f);
        axisFont = new Font("Serif", Font.BOLD, 36);
        areaFont = new Font("Serif", Font.BOLD, 16);
        labelsFont = new Font("Serif", 0, 10);
        GraphicsDisplay.formatter.setMaximumFractionDigits(5);
        addMouseListener(new MouseHandler());
        addMouseMotionListener(new MouseMotionHandler());
    }
    public void showGraphics(Double[][] graphicsData) {
        this.graphicsData = graphicsData;
        repaint();
    }
    public void setShowAxis(boolean showAxis) {
        this.showAxis = showAxis;
        repaint();
    }

    public void setShowArea(boolean showArea){
        this.showArea = showArea;
        repaint();
    }
    public void setShowMarkers(boolean showMarkers) {
        this.showMarkers = showMarkers;
        repaint();
    }
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        double scaleX = getSize().getWidth() / (viewport[1][0] - viewport[0][0]);;
        double scaleY = getSize().getHeight() / (viewport[0][1] - viewport[1][1]);

        scale = Math.min(scaleX, scaleY);
        if (scale==scaleX) {
            double yIncrement = (getSize().getHeight()/scale - (maxY -
                    minY))/2;
            maxY += yIncrement;
            minY -= yIncrement;
        }
        if (scale==scaleY) {
            double xIncrement = (getSize().getWidth()/scale - (maxX -
                    minX))/2;
            maxX += xIncrement;
            minX -= xIncrement;
        }

        if (graphicsData==null || graphicsData.length==0) return;

        Graphics2D canvas = (Graphics2D) g;
        Stroke oldStroke = canvas.getStroke();
        Color oldColor = canvas.getColor();
        Paint oldPaint = canvas.getPaint();
        Font oldFont = canvas.getFont();

        if (Rotate) DoRotate(canvas);
        if(showArea) paintArea(canvas);
        if (showAxis) paintAxis(canvas);
        paintGraphics(canvas);

        if (showMarkers) paintMarkers(canvas);

        canvas.setFont(oldFont);
        canvas.setPaint(oldPaint);
        canvas.setColor(oldColor);
        canvas.setStroke(oldStroke);
        paintLabels(canvas);
        paintSelection(canvas);

    }

    private void paintSelection(Graphics2D canvas) {
        if (!scaleMode) {
            return;
        }
        canvas.setStroke(selectionStroke);
        canvas.setColor(Color.BLACK);
        canvas.draw(selectionRect);
    }

    public void zoomToRegion(double x1, double y1, double x2, double y2) {
        viewport[0][0] = x1;
        viewport[0][1] = y1;
        viewport[1][0] = x2;
        viewport[1][1] = y2;
        repaint();
    }

    protected void DoRotate(Graphics2D canvas) {
//        if(Rotate){
//            double tmp = minX;
//            minX = minY;
//            minY = tmp;
//            tmp = maxX;
//            maxX = maxY;
//            maxY = tmp;
//        }
        Point2D.Double ptr = xyToPoint(0, 0);
        canvas.rotate(3 * Math.PI / 2, ptr.x, ptr.y);
        repaint();
    }

    public void displayGraphics(Double[][] graphicsData) {
        this.graphicsData = graphicsData;
        if (graphicsData==null || graphicsData.length==0) return;
        originalData = new ArrayList<Double[]>(graphicsData.length);
        for (final Double[] point : graphicsData) {
            Double[] newPoint = { new Double(point[0]), new Double(point[1]) };
            this.originalData.add(newPoint);
        }
        minX = graphicsData[0][0];
        maxX = graphicsData[graphicsData.length-1][0];
        minY = graphicsData[0][1];
        maxY = minY;
        for (int i = 1; i<graphicsData.length; i++) {
            if (graphicsData[i][1]<minY) {
                minY = graphicsData[i][1];
            }
            if (graphicsData[i][1]>maxY) {
                maxY = graphicsData[i][1];
            }
        }

        firstlunc = true;
        zoomToRegion(minX, maxY, maxX, minY);
    }
//    protected void paintGraphics(Graphics2D canvas) {
//        Stroke dashed = new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
//                0, new float[]{16, 4, 4, 4, 4, 4, 8, 4, 8, 4}, 0);
//        canvas.setStroke(dashed);
//
//        canvas.setColor(Color.RED);
//        GeneralPath graphics = new GeneralPath();
//
//        for (int i = 0; i<graphicsData.length; i++) {
//            Point2D.Double point = xyToPoint(graphicsData[i][0],
//                    graphicsData[i][1]);
//            if (i>0) {
//                graphics.lineTo(point.getX(), point.getY());
//            } else {
//                graphics.moveTo(point.getX(), point.getY());
//            }
//        }
//        canvas.draw(graphics);
//        canvas.setStroke(graphicsStroke);
//        zoomToRegion(minX, maxY, maxX, minY);
//    }

    private void paintGraphics(Graphics2D canvas) {
        Stroke dashed = new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                0, new float[]{16, 4, 4, 4, 4, 4, 8, 4, 8, 4}, 0);
        canvas.setStroke(dashed);
        canvas.setColor(Color.RED);
        Double currentX = null;
        Double currentY = null;
        for (Double[] point : graphicsData) {
            if (point[0] >= viewport[0][0] && point[1] <= viewport[0][1] && point[0] <= viewport[1][0]) {
                if (point[1] < viewport[1][1]) {
                    continue;
                }
                if (currentX != null && currentY != null) {
                    canvas.draw(new Line2D.Double(xyToPoint(currentX, currentY), xyToPoint(point[0], point[1])));
                }
                currentX = point[0];
                currentY = point[1];
            }
        }
        if(firstlunc){
            zoomToRegion(minX, maxY, maxX, minY);
            firstlunc = false;
        }
    }
    protected void paintMarkers(Graphics2D canvas) {
        canvas.setStroke(markerStroke);
        canvas.setStroke(new BasicStroke(2));
        canvas.setPaint(Color.BLACK);
        Ellipse2D.Double lastMarker = null;
        int i_n = -1;
        for (Double[] point: graphicsData) {
            ++i_n;
            if (point[0] >= viewport[0][0] && point[1] <= viewport[0][1] && point[0] <= viewport[1][0]) {
                if (point[1] < this.viewport[1][1]) {
                    continue;
                }
                Point2D.Double center = xyToPoint(point[0], point[1]);
                Point2D.Double corner = shiftPoint(center, 11, 11);
                boolean sign = true;
                int prev_num = 0;

                String s = String.valueOf(center.y);
                for (int i = 0; i < s.length() / 4; i++) {
                    if (s.charAt(i) == '.') {
                        continue;
                    }
                    if (Integer.valueOf(s.charAt(i)) < prev_num) {
                        sign = false;
                        break;
                    }
                    prev_num = Integer.valueOf(s.charAt(i));
                }
                canvas.setColor(Color.BLACK);
                if (sign) {
                    canvas.setColor(Color.ORANGE);
                }
                Ellipse2D.Double marker = new Ellipse2D.Double();
                marker.setFrameFromCenter(center, corner);

                if (i_n == this.selectedMarker) {
                    lastMarker = marker;
                } else {
                    canvas.draw(new Line2D.Double(center.x - 5.5, center.y - 5.5, center.x + 5.5, center.y + 5.5));
                    canvas.draw(new Line2D.Double(center.x + 5.5, center.y - 5.5, center.x - 5.5, center.y + 5.5));
                    canvas.draw(new Line2D.Double(center.x, center.y - 5.5, center.x, center.y + 5.5));
                    canvas.draw(new Line2D.Double(center.x - 5.5, center.y, center.x + 5.5, center.y));
                }
            }
        }
        if (lastMarker != null) {
            canvas.setColor(Color.BLUE);
            canvas.setPaint(Color.BLUE);
            canvas.draw(lastMarker);
            canvas.fill(lastMarker);
        }
        canvas.setStroke(markerStroke);
    }

    private void paintLabels(final Graphics2D canvas) {
        canvas.setColor(Color.BLACK);
        canvas.setFont(labelsFont);
        FontRenderContext context = canvas.getFontRenderContext();
        if (selectedMarker >= 0) {
            Point2D.Double point = xyToPoint(graphicsData[selectedMarker][0], graphicsData[selectedMarker][1]);
            String label = "X=" + GraphicsDisplay.formatter.format(graphicsData[selectedMarker][0]) +
                    ", Y=" + GraphicsDisplay.formatter.format(graphicsData[selectedMarker][1]);
            Rectangle2D bounds = labelsFont.getStringBounds(label, context);
            canvas.setColor(Color.BLUE);
            canvas.drawString(label, (float)(point.getX() + 5.0), (float)(point.getY() - bounds.getHeight()));
        }
    }

    protected void paintArea(Graphics2D canvas) {
        canvas.setStroke(new BasicStroke(2));
        Double mainY = xyToPoint(0, 0).y;
        FontRenderContext context = canvas.getFontRenderContext();

        int i = 0;
        System.out.println(mainY);
        for(; i < graphicsData.length - 1; i++){
            if(xyToPoint(graphicsData[i][0], graphicsData[i][1]).y > mainY){
                break;
            }
        }

        for(; i < graphicsData.length - 2; i++){
            canvas.setColor(Color.GREEN);
            Double ValueOfarea = 0.d;
            Point2D.Double beginSpot = xyToPoint(graphicsData[i][0], graphicsData[i][1]);
            GeneralPath area = new GeneralPath();
            for(; i < graphicsData.length - 2; i++) {
                if (xyToPoint(graphicsData[i][0], graphicsData[i][1]).y < mainY) {
                    beginSpot = xyToPoint(graphicsData[i][0], graphicsData[i][1]);
                    area.moveTo(beginSpot.x, beginSpot.y);
                    break;
                }
            }
            Point2D.Double endSpot = xyToPoint(graphicsData[i][0], graphicsData[i][1]);
            Point2D.Double highestPoint = xyToPoint(graphicsData[i][0], graphicsData[i][1]);
            for(; i < graphicsData.length - 2; i++) {
                if(highestPoint.y > xyToPoint(graphicsData[i][0], graphicsData[i][1]).y){
                    highestPoint = xyToPoint(graphicsData[i][0], graphicsData[i][1]);
                }
                if (xyToPoint(graphicsData[i][0], graphicsData[i][1]).y > mainY) {
                    i--;
                    endSpot = xyToPoint(graphicsData[i][0], graphicsData[i][1]);
                    break;
                }
                ValueOfarea += ((graphicsData[i - 1][1] + graphicsData[i][1]) / 2) *
                        (graphicsData[i][0] - graphicsData[i - 1][0]);
                Point2D.Double ptr = xyToPoint(graphicsData[i][0], graphicsData[i][1]);
                area.lineTo(ptr.x, ptr.y);
            }
            boolean sign = true;
            for(int i_n = i; i_n < graphicsData.length - 2; i_n++){
                if(xyToPoint(graphicsData[i_n][0], graphicsData[i_n][1]).y > mainY){
                    sign = false;
                }
            }
            if(!sign) {

                area.moveTo(endSpot.x, endSpot.y);
                area.moveTo(beginSpot.x, beginSpot.y);
                area.closePath();
                canvas.draw(area);
                canvas.fill(area);

                canvas.setColor(Color.BLUE);
                canvas.setFont(areaFont);
                String strArea = String.valueOf(ValueOfarea);
                String finalArea = "";
                for(int j = 0; j < 5; j++){
                    finalArea += strArea.charAt(j);
                }
                Rectangle2D bounds = areaFont.getStringBounds(finalArea, context);
                Point2D.Double labelPos = new Point2D.Double();
                labelPos.x = (float)(beginSpot.x + (endSpot.x - beginSpot.x) / 4 - 14);
                labelPos.y = (float)(highestPoint.y - highestPoint.y / 16);
                if(Rotate){
                    labelPos.x = (float)(beginSpot.x + (endSpot.x - beginSpot.x) / 4 - 1);
                    labelPos.y = (float)(highestPoint.y - highestPoint.y / 8);
                }
                if(Rotate){
                    canvas.rotate(Math.PI / 2, labelPos.x, labelPos.y);
                }
                canvas.drawString(finalArea, (float)labelPos.x, (float)labelPos.y);
                if(Rotate){
                    canvas.rotate(3 * Math.PI / 2,  labelPos.x, labelPos.y);
                }
            }
        }
    }
    protected void paintAxis(Graphics2D canvas) {
        canvas.setStroke(axisStroke);
        canvas.setColor(Color.BLACK);
        canvas.setPaint(Color.BLACK);
        canvas.setFont(axisFont);
                FontRenderContext context = canvas.getFontRenderContext();
        if (minX<=0.0 && maxX>=0.0) {
            if(Rotate){
                canvas.draw(new Line2D.Double(xyToPoint(0, maxX),
                        xyToPoint(0, minX)));
            } else {
                canvas.draw(new Line2D.Double(xyToPoint(0, maxY),
                        xyToPoint(0, minY)));
            }
            GeneralPath arrow = new GeneralPath();
            Point2D.Double lineEnd = xyToPoint(0, maxY);
            if(Rotate){
                lineEnd = xyToPoint(0, maxX);
            }

            arrow.moveTo(lineEnd.getX(), lineEnd.getY());
            arrow.lineTo(arrow.getCurrentPoint().getX()+5,
                    arrow.getCurrentPoint().getY()+20);
            arrow.lineTo(arrow.getCurrentPoint().getX()-10,
                    arrow.getCurrentPoint().getY());
            arrow.closePath();
            canvas.draw(arrow);
            canvas.fill(arrow);
            Rectangle2D bounds = axisFont.getStringBounds("y", context);
            Point2D.Double labelPos = xyToPoint(0, maxY);
            if(Rotate){
                labelPos = xyToPoint(minX, 0);
                canvas.rotate(Math.PI / 2, xyToPoint(0, 0).x, xyToPoint(0,0).y);
            }

            canvas.drawString("y", (float)labelPos.getX() + 10,
                    (float)(labelPos.getY() - bounds.getY()));
            if (Rotate){
                canvas.rotate(3 * Math.PI / 2, xyToPoint(0, 0).x, xyToPoint(0,0).y);
            }
        }
        if (minY<=0.0 && maxY>=0.0) {
            if(Rotate){
                canvas.draw(new Line2D.Double(xyToPoint(minY, 0),
                        xyToPoint(maxY, 0)));
            } else {
                canvas.draw(new Line2D.Double(xyToPoint(minX, 0),
                        xyToPoint(maxX, 0)));
            }

            GeneralPath arrow = new GeneralPath();
            Point2D.Double lineEnd = xyToPoint(maxX, 0);
            if(Rotate){
                lineEnd = xyToPoint(maxY, 0);
            }
            arrow.moveTo(lineEnd.getX(), lineEnd.getY());
            arrow.lineTo(arrow.getCurrentPoint().getX()-20,
                    arrow.getCurrentPoint().getY()-5);
            arrow.lineTo(arrow.getCurrentPoint().getX(),
                    arrow.getCurrentPoint().getY()+10);
            arrow.closePath();
            canvas.draw(arrow);
            canvas.fill(arrow);
            Rectangle2D bounds = axisFont.getStringBounds("x", context);
            Point2D.Double labelPos = xyToPoint(maxX, 0);
            if(Rotate){
                labelPos = xyToPoint(0, maxY);
                canvas.rotate(Math.PI / 2, xyToPoint(0, 0).x, xyToPoint(0,0).y);
            }
            canvas.drawString("x", (float)(labelPos.getX() -
                    bounds.getWidth() - 10), (float)(labelPos.getY() + bounds.getY()) + 80);
            if (Rotate){
                canvas.rotate(3 * Math.PI / 2, xyToPoint(0, 0).x, xyToPoint(0,0).y);
            }
        }
    }
    protected Point2D.Double xyToPoint(double x, double y) {
        double deltaX = x - this.viewport[0][0];
        double deltaY = this.viewport[0][1] - y;
        return new Point2D.Double(deltaX*scale, deltaY*scale);
    }

    protected double[] translatePointToXY(int x,    int y) {
        return new double[] { viewport[0][0] + x / scale, viewport[0][1] - y / scale };
    }
    protected Point2D.Double shiftPoint(Point2D.Double src, double deltaX,
                                        double deltaY) {
        Point2D.Double dest = new Point2D.Double();
        dest.setLocation(src.getX() + deltaX, src.getY() + deltaY);
        return dest;
    }

    public void setRotate(boolean rotate) {
        Rotate = rotate;
    }

    protected int findSelectedPoint(int x, int y) {
        if (graphicsData == null) {
            return -1;
        }
        int pos = 0;
        for (Double[] point : graphicsData) {
            Point2D.Double screenPoint = xyToPoint(point[0], point[1]);
            double distance = (screenPoint.getX() - x) * (screenPoint.getX() - x) + (screenPoint.getY() - y)
                    * (screenPoint.getY() - y);
            if (distance < 100.0) {
                return pos;
            }
            ++pos;
        }
        return -1;
    }

    public void reset() {
        Double[][] tmp = new Double[originalData.size()][2];
        for(int i = 0; i < originalData.size(); i++){
            tmp[i][0] = originalData.get(i)[0];
            tmp[i][1] = originalData.get(i)[0];
        }
        displayGraphics(tmp);
    }

    static /* synthetic */ void access$1(GraphicsDisplay graphicsDisplay, double[][] viewport) {
        graphicsDisplay.viewport = viewport;
    }

    static /* synthetic */ void access$6(GraphicsDisplay graphicsDisplay, int selectedMarker) {
        graphicsDisplay.selectedMarker = selectedMarker;
    }

    static /* synthetic */ void access$7( GraphicsDisplay graphicsDisplay, double[] originalPoint) {
        graphicsDisplay.originalPoint = originalPoint;
    }

    static /* synthetic */ void access$9(final GraphicsDisplay graphicsDisplay, final boolean changeMode) {
        graphicsDisplay.changeMode = changeMode;
    }

    static /* synthetic */ void access$10(GraphicsDisplay graphicsDisplay, boolean scaleMode) {
        graphicsDisplay.scaleMode = scaleMode;
    }

    public class MouseHandler extends MouseAdapter implements MouseListener {
        @Override
        public void mouseClicked(MouseEvent ev) {
            if (ev.getButton() == 3) {
                if (GraphicsDisplay.this.undoHistory.size() > 0) {
                    GraphicsDisplay.access$1(GraphicsDisplay.this,
                            GraphicsDisplay.this.undoHistory.get(GraphicsDisplay.this.undoHistory.size() - 1));
                    GraphicsDisplay.this.undoHistory.remove(GraphicsDisplay.this.undoHistory.size() - 1);
                }
                else {
                    GraphicsDisplay.this.zoomToRegion(GraphicsDisplay.this.minX, GraphicsDisplay.this.maxY,
                            GraphicsDisplay.this.maxX, GraphicsDisplay.this.minY);
                }
                GraphicsDisplay.this.repaint();
            }
        }

        @Override
        public void mousePressed(MouseEvent ev) {
            if (ev.getButton() != 1) {
                return;
            }
            GraphicsDisplay.access$6(GraphicsDisplay.this,
                    GraphicsDisplay.this.findSelectedPoint(ev.getX(), ev.getY()));
            GraphicsDisplay.access$7(GraphicsDisplay.this,
                    GraphicsDisplay.this.translatePointToXY(ev.getX(), ev.getY()));
            if (GraphicsDisplay.this.selectedMarker >= 0) {
                GraphicsDisplay.access$9(GraphicsDisplay.this, true);
                GraphicsDisplay.this.setCursor(Cursor.getPredefinedCursor(8));
            }
            else {
                GraphicsDisplay.access$10(GraphicsDisplay.this, true);
                GraphicsDisplay.this.setCursor(Cursor.getPredefinedCursor(5));
                GraphicsDisplay.this.selectionRect.setFrame(ev.getX(), ev.getY(), 1.0, 1.0);
            }
        }

        @Override
        public void mouseReleased(MouseEvent ev) {
            if (ev.getButton() != 1) {
                return;
            }
            GraphicsDisplay.this.setCursor(Cursor.getPredefinedCursor(0));
            if (GraphicsDisplay.this.changeMode) {
                GraphicsDisplay.access$9(GraphicsDisplay.this, false);
            }
            else {
                GraphicsDisplay.access$10(GraphicsDisplay.this, false);
                double[] finalPoint = GraphicsDisplay.this.translatePointToXY(ev.getX(), ev.getY());
                GraphicsDisplay.this.undoHistory.add(GraphicsDisplay.this.viewport);
                GraphicsDisplay.access$1(GraphicsDisplay.this, new double[2][2]);
                GraphicsDisplay.this.zoomToRegion(GraphicsDisplay.this.originalPoint[0], GraphicsDisplay.this.originalPoint[1],
                        finalPoint[0], finalPoint[1]);
                GraphicsDisplay.this.repaint();
            }
        }
    }

    public class MouseMotionHandler implements MouseMotionListener {
        @Override
        public void mouseMoved(MouseEvent ev) {
            GraphicsDisplay.access$6(GraphicsDisplay.this,
                    GraphicsDisplay.this.findSelectedPoint(ev.getX(), ev.getY()));
            if (GraphicsDisplay.this.selectedMarker >= 0) {
                GraphicsDisplay.this.setCursor(Cursor.getPredefinedCursor(8));
            }
            else {
                GraphicsDisplay.this.setCursor(Cursor.getPredefinedCursor(0));
            }
            GraphicsDisplay.this.repaint();
        }

        @Override
        public void mouseDragged(MouseEvent ev) {
            if (GraphicsDisplay.this.changeMode) {
                double[] currentPoint = GraphicsDisplay.this.translatePointToXY(ev.getX(), ev.getY());
                double newY = ((Double[])GraphicsDisplay.this.graphicsData[GraphicsDisplay.this.selectedMarker])[1]
                        + (currentPoint[1] -
                        ((Double[])GraphicsDisplay.this.graphicsData[GraphicsDisplay.this.selectedMarker])[1]);
                if (newY > GraphicsDisplay.this.viewport[0][1]) {
                    newY = GraphicsDisplay.this.viewport[0][1];
                }
                if (newY < GraphicsDisplay.this.viewport[1][1]) {
                    newY = GraphicsDisplay.this.viewport[1][1];
                }
                ((Double[])GraphicsDisplay.this.graphicsData[GraphicsDisplay.this.selectedMarker])[1] = newY;
                GraphicsDisplay.this.repaint();
            }
            else {
                double width = ev.getX() - GraphicsDisplay.this.selectionRect.getX();
                if (width < 5.0) {
                    width = 5.0;
                }
                double height = ev.getY() - GraphicsDisplay.this.selectionRect.getY();
                if (height < 5.0) {
                    height = 5.0;
                }
                GraphicsDisplay.this.selectionRect.setFrame(GraphicsDisplay.this.selectionRect.getX(),
                        GraphicsDisplay.this.selectionRect.getY(), width, height);
                GraphicsDisplay.this.repaint();
            }
        }
    }
}