import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

//Assignment 9 Part 2 
//Koppu Nikitha 
//nkoppu
//Rao Megha 
//megrao920

/*
 * Rules: You have to choose one of the color from the neighbours, starting with the 
 * upper left corner. The color changes and you would have flooded some more area.
 * The goal is to flood the whole grid with the same color in a specified number of steps.
 */

// Represents a single square of the game area
class Cell {
  // In logical coordinates, with the origin at the top-left corner of the
  // screen
  int x;
  int y;
  Color color;
  boolean flooded;
  // the four adjacent cells to this one
  Cell left;
  Cell top;
  Cell right;
  Cell bottom;

  Cell(int x, int y, Color color, boolean flooded) {
    this.x = x;
    this.y = y;
    this.color = color;
    this.flooded = flooded;
    this.left = this;
    this.top = this;
    this.right = this;
    this.bottom = this;
  }

  // renders the cell image
  public WorldImage renderCell() {
    return new OverlayImage(
        new OverlayImage(new EquilateralTriangleImage(FloodItWorld.CELL_SIZE / 2 , 
            OutlineMode.SOLID, Color.BLACK).movePinhole(0, 3), 
            new RotateImage(new EquilateralTriangleImage(FloodItWorld.CELL_SIZE / 2 , 
                OutlineMode.SOLID, Color.black), 180).movePinhole(0, -3)),
        new RectangleImage(FloodItWorld.CELL_SIZE, FloodItWorld.CELL_SIZE, 
            OutlineMode.SOLID, this.color));
  }

  // EFFECT: updates the left cell of the current cell to be the given
  public void setLeft(Cell c) {
    this.left = c;
    c.right = this;
  }

  // EFFECT: updates the right cell of the current cell to be the given
  public void setRight(Cell c) {
    this.right = c;
    c.left = this;
  }

  // EFFECT: updates the top cell of the current cell to be the given
  public void setTop(Cell c) {
    this.top = c;
    c.bottom = this;
  }

  // EFFECT: updates the bottom cell of the current cell to be the given
  public void setBottom(Cell c) {
    this.bottom = c;
    c.top = this;
  }

  // EFFECT: checks and updates the neighbor cells.
  public void checkNeighbours(Color cur) {
    this.color = cur;
    if (this.left.color.equals(cur) && !this.left.flooded) {
      this.left.flooded = true;
      this.left.checkNeighbours(cur);
    }
    if (this.right.color.equals(cur) && !this.right.flooded) {
      this.right.flooded = true;
      this.right.checkNeighbours(cur);
    }
    if (this.bottom.color.equals(cur) && !this.bottom.flooded) {
      this.bottom.flooded = true;
      this.bottom.checkNeighbours(cur);
    }
    if (this.top.color.equals(cur) && !this.top.flooded) {
      this.top.flooded = true;
      this.top.checkNeighbours(cur);
    }
  }

  // checks if this cell is next to a flooded cell
  public boolean nextToFloodedCell() {
    return this.left.flooded || this.right.flooded || this.top.flooded || this.bottom.flooded;
  }

}

// class to represent a world
class FloodItWorld extends World {
  // static final int BOARD_SIZE = 22;
  static final int CELL_SIZE = 23;
  static final ArrayList<Color> loColor = new ArrayList<Color>(
      Arrays.asList(Color.red, Color.blue, Color.pink, Color.yellow, Color.cyan, Color.green));

  Random rand = new Random();

  // All the cells of the game
  ArrayList<Cell> board;
  // player inputed board dimensions
  int gameSize;
  // num of colors in this game (chosen by player)
  int colorRange;
  Color curColor;
  int stepCount = 0;
  int tickCount = 0;

  FloodItWorld(int gameSize, int colorRange) {
    this.gameSize = gameSize;
    this.colorRange = colorRange;
    this.board = boardBuild();
  }

  // initializing an arraylist of cells
  public ArrayList<ArrayList<Cell>> initBoard() {
    ArrayList<ArrayList<Cell>> result = new ArrayList<ArrayList<Cell>>();
    for (int i = 0; i < this.gameSize; i++) {
      ArrayList<Cell> row = new ArrayList<Cell>();
      result.add(row);
      for (int j = 0; j < this.gameSize; j++) {
        int r = rand.nextInt(this.colorRange);
        Cell current = new Cell(i, j, FloodItWorld.loColor.get(r), false);
        result.get(i).add(current);
      }
    }
    result.get(0).get(0).flooded = true;

    for (int i = 0; i < this.gameSize; i++) {
      for (int j = 0; j < this.gameSize; j++) {
        Cell primaryCell = result.get(i).get(j);

        if (primaryCell.y <= 0) {
          primaryCell.setBottom(result.get(i).get(j + 1));
          primaryCell.setTop(primaryCell);
        }
        else {
          if (primaryCell.y >= this.gameSize) {
            primaryCell.setBottom(primaryCell);
            primaryCell.setTop(result.get(i).get(j - 1));
          }
          else {
            primaryCell.setBottom(result.get(i).get(j));
            primaryCell.setTop(result.get(i).get(j - 1));
          }
        }

        if (primaryCell.x <= 0) {
          primaryCell.setRight(result.get(i + 1).get(j));
          primaryCell.setLeft(primaryCell);
        }
        else {
          if (primaryCell.x >= this.gameSize) {
            primaryCell.setRight(primaryCell);
            primaryCell.setLeft(result.get(i - 1).get(j));
          }
          else {
            primaryCell.setRight(result.get(i).get(j));
            primaryCell.setLeft(result.get(i - 1).get(j));
          }
        }
      }
    }
    return result;
  }

  // building the board from a 2d grid
  public ArrayList<Cell> boardBuild() {
    ArrayList<ArrayList<Cell>> loc = this.initBoard();
    this.board = new ArrayList<Cell>();

    for (ArrayList<Cell> arrCell : loc) {
      for (Cell c : arrCell) {
        this.board.add(c);
      }
    }
    this.curColor = this.board.get(0).color;
    return board;
  }

  // EFFECT: changes the cells colors and updates the respective neighbours
  public void changeCells() {
    for (Cell c : this.board) {
      if (c.flooded) {
        c.checkNeighbours(this.curColor);
      }
    }
  }

  // Effect: updates the board on every tick
  public void onTick() {
    tickCount++;
  }

  // Effect: when player clicks any point on the board, board is updated
  public void onMouseClicked(Posn p) {
    int xIndex = (p.x / FloodItWorld.CELL_SIZE);
    int yIndex = (p.y / FloodItWorld.CELL_SIZE);
    Cell curPosn = board.get(((this.gameSize * xIndex) + yIndex));
    if (curPosn.color.equals(this.curColor)) {
      // do nothing
    }
    else if (curPosn.nextToFloodedCell()) {
      this.curColor = curPosn.color;
      curPosn.flooded = true;
      this.stepCount++;
    }
    this.changeCells();
  }

  // Effect: when key is pressed by user, board is updated
  public void onKeyEvent(String ke) {
    if (ke.equals("r")) {
      this.board = boardBuild();
      this.stepCount = 0;
      this.tickCount = 0;
    }
  }

  // returns a single scene that depicts all the cells on the board
  public WorldScene makeScene() {
    WorldScene s = this.getEmptyScene();
    for (Cell c : board) {
      s.placeImageXY(c.renderCell(), c.x * FloodItWorld.CELL_SIZE + 
          (FloodItWorld.CELL_SIZE / 2),
          c.y * FloodItWorld.CELL_SIZE + 
          (FloodItWorld.CELL_SIZE / 2));
    }
    s.height = (int) Math.sqrt(board.size());
    s.width = (int) Math.sqrt(board.size());
    s.placeImageXY(new TextImage("  Step:" + Integer.toString(stepCount) + " / " + 
        Integer.toString((int) (gameSize * 1.75)), 
        20, Color.BLACK), 49,
        27 * FloodItWorld.CELL_SIZE + 17);
    s.placeImageXY(new TextImage("Time:" + Integer.toString(tickCount), 
        20, Color.BLACK),
        (27 * FloodItWorld.CELL_SIZE) - 40, 27 * FloodItWorld.CELL_SIZE + 17);
    this.changeCells();
    return s;
  }

  // determines if the player has won the game
  public boolean win() {
    boolean result = true;
    for (Cell c : this.board) {
      result = result && c.color.equals(curColor);
    }
    return result;
  }

  // determines if the player has lost the game
  public boolean lose() {
    return this.stepCount > ((int) (gameSize * 1.75));
  }

  // method that returns the last scene when the game is over
  public WorldScene lastScene(String msg) {
    WorldScene s = this.makeScene();
    int midPoint = (27 * FloodItWorld.CELL_SIZE) / 2;
    s.placeImageXY(new TextImage(msg, 30, curColor), midPoint, (27 * FloodItWorld.CELL_SIZE) - 50);
    s.placeImageXY(new TextImage("Score:" + Integer.toString(this.stepCount * this.tickCount), 20,
        Color.black), (13 * FloodItWorld.CELL_SIZE), (27 * FloodItWorld.CELL_SIZE) + 17);
    return s;
  }

  // method that renders the world scene when the game ends with the
  // appropriate message
  public WorldEnd worldEnds() {
    if (this.win()) {
      return new WorldEnd(true, this.lastScene("YOU WON!"));
    }
    else if (this.lose()) {
      return new WorldEnd(true, this.lastScene("YOU LOST!"));
    }
    else {
      return new WorldEnd(false, this.makeScene());
    }
  }
}

// Examples class to test Game
class ExamplesFloodIt {
  FloodItWorld f1;
  Cell cellTop;
  Cell cellBottom;
  Cell cellRight;
  Cell cellLeft;
  Cell c1;
  Cell c2;
  Cell c3;
  WorldScene s;

  void initData() {
    this.f1 = new FloodItWorld(22,6);
    //maximum number of colors is 6 ^
    this.cellTop = new Cell(10, 10, Color.blue, false);
    this.cellBottom = new Cell(9, 9, Color.blue, false);
    this.cellRight = new Cell(8, 8, Color.GRAY, false);
    this.cellLeft = new Cell(7, 7, Color.green, false);
    this.c1 = new Cell(50, 1, Color.red, true);
    this.c2 = new Cell(9, 8, Color.cyan, true);
    this.c3 = new Cell(22, 8, Color.gray, false);
  }

  // test for initBoard
  void testInitBoard(Tester t) {
    this.initData();
    t.checkExpect(f1.initBoard().get(0).get(0).x, 0);
    t.checkExpect(f1.initBoard().get(1).get(1).x, 1);
    t.checkExpect(f1.initBoard().get(2).get(3).y, 3);
    t.checkExpect(f1.initBoard().get(1).get(0).y, 0);
    t.checkExpect(f1.initBoard().size(), f1.gameSize);
    t.checkExpect(f1.initBoard().get(0).size(), f1.gameSize);
    // t.checkExpect(f1.initBoard().get(0).get(0).color
    // .equals(f1.initBoard().get(0).get(f1.gameSize - 1).color)
    // || f1.initBoard().get(0).get(0).color
    // .equals(f1.initBoard().get(0).get(f1.gameSize - 2).color)
    // || f1.initBoard().get(0).get(0).color
    // .equals(f1.initBoard().get(0).get(f1.gameSize - 3).color)
    // || f1.initBoard().get(0).get(0).color
    // .equals(f1.initBoard().get(f1.gameSize).get(0).color), true);
  }

  // test for boardBuild
  void testBoardBuild(Tester t) {
    this.initData();
    t.checkExpect(f1.boardBuild().size(), f1.gameSize * f1.gameSize);
    t.checkExpect(f1.boardBuild().get(0).color.equals(f1.boardBuild().get(4)), false);
    t.checkExpect(f1.curColor, f1.board.get(0).color);
  }

  // test for renderCell
  void testRenderCell(Tester t) {
    this.initData();
    t.checkExpect(c1.renderCell(), 
        new OverlayImage(
            new OverlayImage(new EquilateralTriangleImage(FloodItWorld.CELL_SIZE / 2 , 
                OutlineMode.SOLID, Color.BLACK).movePinhole(0, 3), 
                new RotateImage(new EquilateralTriangleImage(FloodItWorld.CELL_SIZE / 2 , 
                    OutlineMode.SOLID, Color.black), 180).movePinhole(0, -3)),
            new RectangleImage(FloodItWorld.CELL_SIZE, FloodItWorld.CELL_SIZE, 
                OutlineMode.SOLID, c1.color)));
  }

  //test for nextToFloodedCell
  void testNextToFloodedCell(Tester t) {
    this.initData();
    cellBottom.setLeft(c1);
    c2.setBottom(c3);
    t.checkExpect(cellBottom.nextToFloodedCell(), true);
    cellLeft.setBottom(c3);
    t.checkExpect(cellLeft.nextToFloodedCell(), false);
  }

  // test for setLeft, setRight, setTop and setBottom
  // we put it in the same method because the methods
  // do the same thing but for different types of cells.
  void testSet(Tester t) {
    this.initData();
    t.checkExpect(c1.bottom, c1);
    c1.setTop(c2);
    c2.setRight(c1);
    c1.setLeft(cellTop);
    c2.setBottom(c1);

    t.checkExpect(c1.top, c2);
    t.checkExpect(c2.right, c1);
    t.checkExpect(c1.left, cellTop);
    t.checkExpect(c2.bottom, c1);
  }

  // test for makeScene
  void testMakeScene(Tester t) {
    this.initData();
    s = f1.makeScene();
    t.checkExpect(s.height, f1.gameSize);
    t.checkExpect(s.width, f1.gameSize);
  }

  // test for win
  void testWin(Tester t) {
    this.initData();
    t.checkExpect(f1.win(), false);
    for (Cell c : f1.board) {
      c.color = f1.curColor;
    }
    t.checkExpect(f1.win(), true);
  }

  // test for lose
  void testLose(Tester t) {
    this.initData();
    t.checkExpect(f1.lose(), false);
    f1.stepCount = 245;
    t.checkExpect(f1.lose(), true);
  }

  // test for worldEnds
  void testWorldEnds(Tester t) {
    this.initData();
    t.checkExpect(f1.worldEnds(), new WorldEnd(false, f1.makeScene()));
    f1.stepCount = 245;
    t.checkExpect(f1.worldEnds(), new WorldEnd(true, f1.lastScene("YOU LOST!")));
    for (Cell c : f1.board) {
      c.color = f1.curColor;
    }
    t.checkExpect(f1.worldEnds(), new WorldEnd(true, f1.lastScene("YOU LOST!")));
    this.initData();
    for (Cell c : f1.board) {
      c.color = f1.curColor;
    }
    t.checkExpect(f1.worldEnds(), new WorldEnd(true, f1.lastScene("YOU WON!")));
  }

  // test for onKeyEvent
  void testOnKey(Tester t) {
    this.initData();
    ArrayList<Cell> temp = f1.board;
    f1.onKeyEvent("r");
    t.checkExpect(temp.get(0) != f1.board.get(0), true);
  }

  // test for checkNeighbours
  void testCheckNeighbours(Tester t) {
    this.initData();
    t.checkExpect(this.cellLeft.flooded, false);
    this.cellLeft.checkNeighbours(Color.green);
    t.checkExpect(this.cellLeft.flooded, true);

    t.checkExpect(this.cellRight.flooded, false);
    this.cellRight.checkNeighbours(Color.gray);
    t.checkExpect(this.cellRight.flooded, true);

    t.checkExpect(this.cellTop.flooded, false);
    this.cellTop.checkNeighbours(Color.blue);
    t.checkExpect(this.cellTop.flooded, true);

    t.checkExpect(this.cellBottom.flooded, false);
    this.cellBottom.checkNeighbours(Color.blue);
    t.checkExpect(this.cellBottom.flooded, true);
  }

  // test for changeCells
  void testChangeCells(Tester t) {
    this.initData();
    f1.changeCells();
    t.checkExpect(f1.boardBuild().get(0).flooded, true);
  }

  // test for onTick
  void testOnTick(Tester t) {
    this.initData();
    t.checkExpect(f1.tickCount, 0);
    f1.onTick();
    t.checkExpect(f1.tickCount, 1);
    f1.onTick();
    t.checkExpect(f1.tickCount, 2);
  }

  // test for onMouseClicked
  void testOnMouseClicked(Tester t) {
    this.initData();
    f1.curColor = f1.board.get(0).color;
    t.checkExpect(f1.stepCount, 0);
    f1.onMouseClicked(new Posn(0, 13));
    int xIndex = (0 / FloodItWorld.CELL_SIZE);
    int yIndex = (13 / FloodItWorld.CELL_SIZE);
    Cell curPosn = f1.board.get(((f1.gameSize * xIndex) + yIndex));

    t.checkExpect(f1.curColor, curPosn.color);
    t.checkExpect(curPosn.flooded, true);
  }

  // test for lastScene
  void testLastScene(Tester t) {
    this.initData();
    f1.lastScene("YOU WON!");

    WorldScene s = f1.makeScene();
    int midPoint = (27 * FloodItWorld.CELL_SIZE) / 2;
    s.placeImageXY(new TextImage("YOU WON!", 28, Color.BLACK), midPoint, midPoint);
    s.placeImageXY(
        new TextImage("Score:" + Integer.toString(f1.stepCount * f1.tickCount), 20, Color.black),
        (13 * FloodItWorld.CELL_SIZE), (27 * FloodItWorld.CELL_SIZE) + 17);
    t.checkExpect(f1.lastScene("YOU WON!"), s);

    f1.lastScene("YOU LOST!");
    WorldScene sc = f1.makeScene();
    s.placeImageXY(new TextImage("YOU LOST!", 28, Color.BLACK), midPoint, midPoint);
    s.placeImageXY(
        new TextImage("Score:" + Integer.toString(f1.stepCount * f1.tickCount), 20, Color.black),
        (13 * FloodItWorld.CELL_SIZE), (27 * FloodItWorld.CELL_SIZE) + 17);
    t.checkExpect(f1.lastScene("YOU LOST!"), sc);
  }

  // test that runs the game
  void testRunGame(Tester t) {
    this.initData();
    f1.bigBang(27 * FloodItWorld.CELL_SIZE, (27 * FloodItWorld.CELL_SIZE) + 30, 1);
  }
}
