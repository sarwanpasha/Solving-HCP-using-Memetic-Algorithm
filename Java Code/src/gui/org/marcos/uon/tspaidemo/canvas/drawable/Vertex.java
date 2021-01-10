package org.marcos.uon.tspaidemo.canvas.drawable;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.marcos.uon.tspaidemo.canvas.TransformationContext;

public class Vertex implements Drawable {
    private Point2D location;
    private double dotRadius;
    private double strokeWidth;
    private String label;
    private Color dotFill;
    private Color labelFill;
    private Color dotStroke;

    public Vertex(Point2D location, double radius, double strokeWidth, String label, Color dotFill, Color dotStroke, Color labelFill) {
        this.location = location;
        this.dotRadius = radius;
        this.strokeWidth = strokeWidth;
        this.label = label;
        this.dotFill = dotFill;
        this.dotStroke = dotStroke;
        this.labelFill = labelFill;
    }

    public Vertex(double x, double y, double radius, double strokeWidth, String label, Color dotFill, Color dotStroke, Color labelFill) {
        this(new Point2D(x, y), radius, strokeWidth, label, dotFill, dotStroke, labelFill);
    }

    public Point2D getLocation() {
        return location;
    }

    public void setLocation(Point2D location) {
        this.location = location;
    }

    public double getDotRadius() {
        return dotRadius;
    }

    public void setDotRadius(double dotRadius) {
        this.dotRadius = dotRadius;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Color getDotFill() {
        return dotFill;
    }

    public void setDotFill(Color dotFill) {
        this.dotFill = dotFill;
    }

    public Color getLabelFill() {
        return labelFill;
    }

    public void setLabelFill(Color labelFill) {
        this.labelFill = labelFill;
    }

    public double getStrokeWidth() {
        return strokeWidth;
    }

    public void setStrokeWidth(double strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    public Color getDotStroke() {
        return dotStroke;
    }

    public void setDotStroke(Color dotStroke) {
        this.dotStroke = dotStroke;
    }

    @Override
    public void drawOnto(GraphicsContext graphicsContext, TransformationContext transformationContext) {
        double lineWidthToUse = transformationContext.computeStrokeWidthToUse(strokeWidth);
        double radiusToUse = transformationContext.computeRadiusToUse(dotRadius);
        graphicsContext.setFill(dotFill);
        Point2D minCorner = transformationContext.localToCanvas(location).subtract(new Point2D(radiusToUse, radiusToUse));
        graphicsContext.fillOval(minCorner.getX(), minCorner.getY(), radiusToUse*2, radiusToUse*2);
        graphicsContext.setLineWidth(lineWidthToUse);
        graphicsContext.setStroke(dotStroke);
        graphicsContext.strokeOval(minCorner.getX(), minCorner.getY(), radiusToUse*2, radiusToUse*2);
    }
}
