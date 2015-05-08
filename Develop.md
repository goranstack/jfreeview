## Step by step how to getting started ##

  1. Install Eclipse IDE for Java Developers from: http://www.eclipse.org/downloads/
  1. Install the Subclipse plug-in from: http://subclipse.tigris.org/
  1. Install the M2Eclipse plug-in from: http://m2eclipse.sonatype.org/
  1. Open the SVN Repository Exploring Perspective in Eclipse
  1. Press the "Add SVN Repository" button and add the following URL in the dialog: http://jfreeview.googlecode.com/svn/trunk
  1. Select the jfreeview-parent folder and use the right button menu to perform: Checkout as Maven Project...

Your Eclipse workspace should now be populated with the modules of the JFreeView project.
The dependency jars should be automatically downloaded by Maven.

Go to the Java perspective and run for example SVGRasterizerTest. The application is demo of the SVG rendering in the Batik framework. If a window showing a series of SVG images is displayed you have successfully checked out the source code and configured your Eclipse.

![http://jfreeview.googlecode.com/svn/site/images/svg-rasterizer-test.png](http://jfreeview.googlecode.com/svn/site/images/svg-rasterizer-test.png)