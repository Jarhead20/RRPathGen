package jarhead;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.path.Path;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;

import java.nio.IntBuffer;

public class Render {
    protected static void setup( GL2 gl2, int width, int height ) {
        gl2.glMatrixMode( GL2.GL_PROJECTION );
        gl2.glLoadIdentity();

        // coordinate system origin at lower left with width and height same as the window
        GLU glu = new GLU();
        glu.gluOrtho2D( 0.0f, width, 0.0f, height );

        gl2.glMatrixMode( GL2.GL_MODELVIEW );
        gl2.glLoadIdentity();

        gl2.glViewport( 0, 0, width, height );
    }

    protected static void render(GL2 gl2, int width, int height, Path path, double resolution, double rX, double rY) {
        gl2.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

//        int v = gl2.glCreateShader(GL2.GL_VERTEX_SHADER);
//        gl2.glShaderSource(v, 1, new String[]{""}, IntBuffer.wrap(null));
//        gl2.glUniformMatrix3fv(0, );
        // draw a triangle filling the window


        if(path != null){
            gl2.glColor3f(0.574f, 0.343f, 0.671f);
            gl2.glLoadIdentity();

//            Pose2d pose = null;
            for (double i = 0; i < path.length(); i+=resolution) {
                gl2.glBegin( GL2.GL_QUADS );
                Pose2d pose1 = path.get(i);

                double theta = -pose1.getHeading()+ Math.toRadians(90);
                double x1 = pose1.getX();
                double y1 = pose1.getY();

                gl2.glVertex2d((x1+((Math.cos(theta)*rX) - (rY*Math.sin(theta)))),((height-y1)+((Math.sin(theta)*rX) + (rY*Math.cos(theta)))));
                gl2.glVertex2d((x1-((Math.cos(theta)*rX) + (rY*Math.sin(theta)))),((height-y1)-((Math.sin(theta)*rX) - (rY*Math.cos(theta)))));


                gl2.glVertex2d((x1-((Math.cos(theta)*rX) - (rY*Math.sin(theta)))),((height-y1)-((Math.sin(theta)*rX) + (rY*Math.cos(theta)))));
                gl2.glVertex2d((x1+((Math.cos(theta)*rX) + (rY*Math.sin(theta)))),((height-y1)+((Math.sin(theta)*rX) - (rY*Math.cos(theta)))));
                gl2.glEnd();
            }

//            for (double i = path.length()-resolution; i >=0; i-=resolution) {
//                pose = path.get(i);
//                double x = pose.getX();
//                double y = pose.getY();
//                double theta = -pose.getHeading()+ Math.toRadians(90);
//                gl2.glVertex2d((x-(Math.cos(theta)*rX)),((height-y)-(Math.sin(theta)*rY)));
//
//            }
//
//            if(pose != null){
//                double theta = -pose.getHeading()+ Math.toRadians(90);
//                gl2.glVertex2d((pose.getX()+(Math.cos(theta)*rX)),((height-pose.getY())+(Math.sin(theta)*rY)));
//            }





            gl2.glColor3f(1f, 0f, 0f);
            gl2.glBegin(GL2.GL_LINE_STRIP);

            for (double i = 0; i < path.length(); i+=resolution) {
                Pose2d pose = path.get(i);
                double x = pose.getX();
                double y = pose.getY();
//            double theta = pose.getHeading();
                gl2.glVertex2f((float)x,(float)(height-y));
            }

            gl2.glEnd();
        }




    }
}