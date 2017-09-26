package com.dmsys.airdiskpro.view.loadingBall.factory;

import android.graphics.Path;
import android.graphics.Point;

public class Circle extends BallPath {

  public Circle(Point center, int pathWidth, int pathHeight, int maxBallSize) {
    super(center, pathWidth, pathHeight, maxBallSize);
  }

  @Override
  public Path draw() {
    Path path = new Path();
    path.addCircle(center.x, center.y, pathWidth / 2 - maxBallSize, Path.Direction.CCW);
    return path;
  }
}
