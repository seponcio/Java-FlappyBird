package flappyBird;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.Timer;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem; 


public class FlappyBird implements ActionListener, MouseListener, KeyListener
{
	public static FlappyBird flappyBird;
	private final int WIDTH = 1200;  
	private final int HEIGHT = 800;
	private final int REFRESH_RATE_MS = 40; 
	
	private Renderer renderer;
	private Rectangle bird;	
	private ArrayList<Rectangle> columns;
	private Random rand;
	private int ticks;
	private int yMotion;
	private int score;
	private GameState gameIs;
	
	//constructor for the FlappyBird class
	public FlappyBird()
	{		
		rand = new Random();
		gameIs = GameState.READY;
		
		setFrame();		
		resetBird();
		
		columns = new ArrayList<Rectangle>();
		
		for (int i =  1; i <= 4 ; i++)
		{
			addColumn(true);
		}
		
		Timer timer = new Timer(REFRESH_RATE_MS, this);	
		timer.start();
	}
	
	//sets up configuration for the application's main JFrame
	private void setFrame()
	{
		JFrame jframe = new JFrame();	
		renderer = new Renderer();
		jframe.add(renderer);
		jframe.setTitle("Flappy Bird");
		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jframe.setSize(WIDTH, HEIGHT);
		jframe.addMouseListener(this);
		jframe.addKeyListener(this);
		jframe.setResizable(false);
		jframe.setVisible(true);
	}
	
	public void addColumn(boolean start)
	{
		int space = 300;
		int width = 100;
		int height = 50 + rand.nextInt(300);
		
		if (start)
		{
			columns.add(new Rectangle(WIDTH + width + columns.size() * 300, HEIGHT - height - 120, width, height ));
			columns.add(new Rectangle(WIDTH + width + (columns.size() - 1) * 300, 0, width, HEIGHT - height - space));
		}
		else
		{
			columns.add(new Rectangle(columns.get(columns.size() - 1).x + 600, HEIGHT - height - 120, width, height ));
			columns.add(new Rectangle(columns.get(columns.size() - 1).x, 0, width, HEIGHT - height - space));
		}
	}
	
	public void paintColumn(Graphics g, Rectangle column)
	{
		//System.out.println("painting column");
		g.setColor(Color.GREEN.darker());
		g.fillRect(column.x, column.y, column.width, column.height);
	}
	
	//action to perform each time the bird jumps
	public void jump()
	{		 
		if (gameIs == GameState.READY)
		{
			gameIs = GameState.STARTED;
		}
		else if (gameIs == GameState.STARTED)
		{
			if (yMotion > 0)
			{
				yMotion = 0;
			}
			yMotion -= 10;
		}		
		else if (gameIs == GameState.OVER)
		{
			resetBird();
			
			columns.clear();
			yMotion = 0;
			score = 0;
			
			for (int i =  1; i <= 4 ; i++)
			{
				addColumn(true);
			}
			
			gameIs = GameState.READY;
		}
	 }
	
	private void resetBird()
	{
		final int BIRD_SIZE = 20;
		final Point BIRD_INITIAL_LOCATION = new Point((WIDTH / 2 - BIRD_SIZE / 2), (HEIGHT / 2 - BIRD_SIZE / 2));
		
		bird = new Rectangle(BIRD_INITIAL_LOCATION.x , BIRD_INITIAL_LOCATION.y, BIRD_SIZE, BIRD_SIZE);
	}
	 
	@Override
	//Defines the action to be performed by the ActionListener trigger
	//This action is triggered by the timer event
	public void actionPerformed(ActionEvent e)
	{
		ticks++;
		
		final int speed = 10;
		final int maxVerticalDropSpeed = 10;
		
		//if (gameIs == GameState.STARTED || gameIs == GameState.OVER)
		if (gameIs == GameState.STARTED)
		{
			//calculate bird fall
			if (	(ticks % 2 == 0)
				&& 	(yMotion < maxVerticalDropSpeed)
				)
			{				
				yMotion += 2;
			}
			
			//adjusts the bird vertical position
			bird.y += yMotion;
			
			//push all columns to the left
			for (int i = 0; i < columns.size(); i++)
			{
				Rectangle column = columns.get(i);	
				column.x -= speed; 
			}
			
			for (int i = 0; i < columns.size(); i++)
			{
				Rectangle column = columns.get(i);	

				//remove columns once they cross the left border of the screen
				if (column.x + column.width < 0)
				{
					if (column.y == 0)
					{
						addColumn(false);
					}
					columns.remove(column);
				}
			}
			
			for (Rectangle column : columns)
			{
				if (	(column.y == 0)
					&& 	((bird.x + bird.width / 2 ) > (column.x + column.width / 2 - 10))
					&& 	((bird.x + bird.width / 2 ) < (column.x + column.width / 2 + 10)) 
					)
				{
					score++;
				}				
				else if (column.intersects(bird))
				{
					birdHitsWall(column);
				}
			}
			
			boolean birdTooHigh = (bird.y < 0);
			boolean birdHitsGround = (bird.y + yMotion) >= (HEIGHT - 120);
			
			if (birdTooHigh || birdHitsGround)
			{
				gameIs = GameState.OVER;
			}
			
			if (birdHitsGround)
			{
				int grounded = HEIGHT - 120 - bird.height;
				bird.y = grounded;
			}
		}
		
		renderer.repaint();		
	}
	
	private void birdHitsWall(Rectangle column)
	{
		boolean fromLeft 		= (bird.x <= column.x);
		boolean fromAbove 		= (column.y != 0);
		boolean fromUnderneath 	= (bird.y < column.height);
		
		gameIs = GameState.OVER;
		
		if (fromLeft)
		{
			bird.x = column.x - bird.width;	
		}
		else if (fromAbove)
		{
			bird.y = column.y - bird.height;
		}
		else if (fromUnderneath)
		{
			bird.y = column.height;
		}	
	}
	
	
	public void repaint(Graphics g)
	{
		//Sky
		g.setColor(Color.CYAN);
		g.fillRect(0,0, WIDTH, HEIGHT);
		
		//dirt
		g.setColor(Color.ORANGE);
		g.fillRect(0, (HEIGHT - 120), WIDTH, 120);
		
		//grass
		g.setColor(Color.GREEN);
		g.fillRect(0, (HEIGHT - 120), WIDTH, 20);
		
		//bird
		g.setColor(Color.RED);
		g.fillRect(bird.x, bird.y, bird.width, bird.height);
		
		for (Rectangle column : columns)
		{
			paintColumn(g, column);
		}
		
		//hud
		g.setColor(Color.WHITE);	
		g.setFont(new Font("Arial", 1, 100));
		
		if (gameIs == GameState.OVER)
		{
			g.drawString("Game Over", 100, HEIGHT / 2 - 50);
		}
		else if (gameIs == GameState.READY)
		{
			g.drawString("Click to start!", 75, HEIGHT / 2 - 50);
		}		
		else if (gameIs == GameState.STARTED)
		{
			g.setFont(new Font("Arial", 1, 50));
			g.drawString("Score: " + String.valueOf(score), 25, 50);
		}
		
		//debugging info INI
		g.setFont(new Font("Arial", 1, 20));
		g.setColor(Color.BLACK);
		g.drawString("Tick: " 					+ String.valueOf(ticks), 			800, 50);
		g.drawString("yMotion: " 				+ String.valueOf(yMotion), 			800, 70);
		g.drawString("Bird.y: " 				+ String.valueOf(bird.y), 			800, 90);
		g.drawString("State: " 					+ String.valueOf(gameIs), 			800, 110);
		g.drawString("columns array size: " 	+ String.valueOf(columns.size()), 	800, 130);
		
		//debugging info END
	}
	public static void main(String[] args)
	{
		flappyBird = new FlappyBird();
	}

	@Override
	public void keyReleased(KeyEvent e)
	{
		if (e.getKeyCode() == KeyEvent.VK_SPACE)
		{
			jump();
		}
	}
	
	@Override
	public void mouseClicked(MouseEvent e)
	{
		jump();
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}


}
