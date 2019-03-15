import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.EventHandler;
import javafx.scene.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.stage.Stage;

import java.awt.*;

public class Main extends Application {
    private static final int WIDTH = 1400;
    private static final int HEIGHT = 1000;
    private double anchorX, anchorY;
    private double anchorAngleX = 0;
    private double anchorAngleY = 0;
    private final DoubleProperty angleX = new SimpleDoubleProperty(0);
    private final DoubleProperty angleY = new SimpleDoubleProperty(0);

    final SmartGroup boxGroup = new SmartGroup();
    final Group root = new Group();
    final Group axisGroup = new Group();
    final Xform world = new Xform();
    final PerspectiveCamera camera = new PerspectiveCamera();
    final Xform cameraXform = new Xform();
    final Xform cameraXform2 = new Xform();
    final Xform cameraXform3 = new Xform();
    final double cameraDistance = 1000;

    double mousePosX;
    double mousePosY;
    double mouseOldX;
    double mouseOldY;
    double mouseDeltaX;
    double mouseDeltaY;

    private void buildScene(){
        root.getChildren().add(world);
    }

    private void buildBox() {
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(Color.DARKBLUE);
        material.setSpecularColor(Color.BLUE);
        Box box = new Box(100, 20, 50);
        box.setMaterial(material);
        boxGroup.getChildren().addAll(box);
        world.getChildren().addAll(boxGroup);
    }

    private void buildAxes() {
        final PhongMaterial redMaterial = new PhongMaterial();
        redMaterial.setDiffuseColor(Color.DARKRED);
        redMaterial.setSpecularColor(Color.RED);

        final PhongMaterial greenMaterial = new PhongMaterial();
        greenMaterial.setDiffuseColor(Color.DARKGREEN);
        greenMaterial.setSpecularColor(Color.GREEN);

        final PhongMaterial yellowMaterial = new PhongMaterial();
        yellowMaterial.setDiffuseColor(Color.YELLOW);
        yellowMaterial.setSpecularColor(Color.YELLOW);

        final Box xAxis = new Box(1000.0, 1, 1);
        final Box yAxis = new Box(1, 1000.0, 1);
        final Box zAxis = new Box(1, 1, 1000.0);

        xAxis.setMaterial(redMaterial);
        yAxis.setMaterial(greenMaterial);
        zAxis.setMaterial(yellowMaterial);

        axisGroup.getChildren().addAll(xAxis, yAxis, zAxis);
        world.getChildren().addAll(axisGroup);
    }

    private void buildCamera() {
        root.getChildren().add(cameraXform);
        cameraXform.getChildren().add(cameraXform2);
        cameraXform2.getChildren().add(cameraXform3);
        cameraXform3.getChildren().add(camera);
//        cameraXform3.setRotateZ(0.0);

        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        camera.setTranslateZ(-cameraDistance);
//        cameraXform.ry.setAngle(20.0);
//        cameraXform.rx.setAngle(40);
    }

    private void handleMouse(Scene scene, final Node root) {
        scene.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent me) {
                mousePosX = me.getSceneX();
                mousePosY = me.getSceneY();
                mouseOldX = me.getSceneX();
                mouseOldY = me.getSceneY();
            }
        });
        scene.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent me) {
                mouseOldX = mousePosX;
                mouseOldY = mousePosY;
                mousePosX = me.getSceneX();
                mousePosY = me.getSceneY();
                mouseDeltaX = (mousePosX - mouseOldX);
                mouseDeltaY = (mousePosY - mouseOldY);

                double modifier = 1.0;
                double modifierFactor = 0.1;

                if (me.isControlDown()) {
                    modifier = 0.1;
                }
                if (me.isShiftDown()) {
                    modifier = 10.0;
                }
                if (me.isPrimaryButtonDown()) {
                    cameraXform.ry.setAngle(cameraXform.ry.getAngle() - mouseDeltaX*modifierFactor*modifier*2.0);  // +
                    cameraXform.rx.setAngle(cameraXform.rx.getAngle() + mouseDeltaY*modifierFactor*modifier*2.0);  // -
                }
                else if (me.isSecondaryButtonDown()) {
                    double z = camera.getTranslateZ();
                    double newZ = z + mouseDeltaX*modifierFactor*modifier;
                    camera.setTranslateZ(newZ);
                }
                else if (me.isMiddleButtonDown()) {
                    cameraXform2.t.setX(cameraXform2.t.getX() + mouseDeltaX*modifierFactor*modifier*0.3);  // -
                    cameraXform2.t.setY(cameraXform2.t.getY() + mouseDeltaY*modifierFactor*modifier*0.3);  // -
                }
            }
        });
    }

    private Group mouseControl(Group group, Scene scene){
        Rotate xRotate;
        Rotate yRotate;
        group.getTransforms().addAll(
                xRotate = new Rotate(0, Rotate.X_AXIS),
                yRotate = new Rotate(0, Rotate.Y_AXIS)
        );
        xRotate.angleProperty().bind(angleX);
        yRotate.angleProperty().bind(angleY);

        scene.setOnMousePressed(event -> {
            anchorX = event.getSceneX();
            anchorY = event.getSceneY();
            anchorAngleX = angleX.get();
            anchorAngleY = angleY.get();
        });

        scene.setOnMouseDragged(event -> {
            angleX.set(anchorAngleX - (anchorY - event.getSceneY()));
            angleY.set(anchorAngleY + anchorX - event.getSceneX());
        });



        return group;
    }


    @Override
    public void start(Stage primaryStage) {
        buildScene();
        buildAxes();

        buildCamera();

        Scene scene = new Scene(root, WIDTH, HEIGHT, true);
        scene.setFill(Color.GREY);

//        boxGroup.translateXProperty().set(WIDTH/2);
//        boxGroup.translateYProperty().set(HEIGHT/2);
//        boxGroup.translateZProperty().set(-1500);

        world.translateXProperty().set(WIDTH/2);
        world.translateYProperty().set(HEIGHT/2);
        world.translateZProperty().set(-1000);
//        handleMouse(scene, boxGroup);

//        primaryStage.addEventHandler(MouseEvent.MOUSE_CLICKED, event ->{
//            mouseControl(boxGroup, scene);
//        });

        primaryStage.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            switch (event.getCode()) {
                case U:
                    boxGroup.translateXProperty().set(boxGroup.getTranslateX() + 10);
                    break;
                case I:
                    boxGroup.translateXProperty().set(boxGroup.getTranslateX() - 10);
                    break;
                case P:
                    boxGroup.translateYProperty().set(boxGroup.getTranslateY() + 10);
                    break;
                case O:
                    boxGroup.translateYProperty().set(boxGroup.getTranslateY() - 10);
                    break;
                case K:
                    boxGroup.translateZProperty().set(boxGroup.getTranslateZ() + 100);
                    break;
                case L:
                    boxGroup.translateZProperty().set(boxGroup.getTranslateZ() - 100);
                    break;
                case W:
                    boxGroup.shearByX();
                    break;
                case X:
                    buildBox();
                    break;
                case V:
                    mouseControl(boxGroup, scene);
                    break;
                case B:
                    mouseControl(axisGroup, scene);
                    break;

//                case Q:
//                    boxGroup.rotateByX(10);
//                    break;
//                case E:
//                    boxGroup.rotateByX(-10);
//                    break;
//                case A:
//                    boxGroup.rotateByY(10);
//                    break;
//                case D:
//                    boxGroup.rotateByY(-10);
//                    break;
//                case Z:
//                    boxGroup.rotateByZ(10);
//                    break;
//                case C:
//                    boxGroup.rotateByZ(-10);
//                    break;
            }
        });

        primaryStage.setTitle("3D Transformations");
        primaryStage.setScene(scene);
        primaryStage.show();
        scene.setCamera(camera);
    }

    public static void main(String[] args) {
        launch(args);
    }

}
