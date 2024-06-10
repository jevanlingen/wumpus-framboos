import { AfterViewInit, Component, ElementRef, Input, ViewChild } from '@angular/core';
import { Coordinate, Game } from '../../model/game';

@Component({
  selector: 'app-game-canvas',
  standalone: true,
  imports: [],
  templateUrl: './game-canvas.component.html',
  styleUrl: './game-canvas.component.css'
})
export class GameCanvasComponent implements AfterViewInit {

  @Input({ required: true })
  game!: Game;

  @ViewChild('c')
  canvas!: ElementRef<HTMLCanvasElement>;
  context!: CanvasRenderingContext2D;

  ngAfterViewInit(): void {
    const x = this.canvas.nativeElement.getContext('2d');
    if (x) {
      this.context = x;
      this.drawBoard();
      this.addWumpus();
      this.addPits();
      this.addGold();
      this.addPlayers();
    }
  }

  drawBoard() {
    const { boxWidth, canvasHeight, boxHeight, canvasWidth } = this.getCanvasProps();

    for (let index = 0; index <= this.game.gridSize; index++) {
      this.context.moveTo(0.5 + (index * boxWidth), 0);
      this.context.lineTo(0.5 + (index * boxWidth), canvasHeight);
    }

    for (let index = 0; index <= this.game.gridSize; index++) {
      this.context.moveTo(0, 0.5 + (index * boxHeight));
      this.context.lineTo(canvasWidth, 0.5 + (index * boxHeight));
    }

    this.context.strokeStyle = 'black';
    this.context.stroke();
  }

  private getCanvasProps() {
    const canvasWidth = this.canvas.nativeElement.width - 0.5;
    const canvasHeight = this.canvas.nativeElement.height - 0.5;

    const boxWidth = canvasWidth / this.game.gridSize;
    const boxHeight = canvasHeight / this.game.gridSize;
    return { boxWidth, canvasHeight, boxHeight, canvasWidth };
  }

  addWumpus() {
    const c = this.mapCoordinatesToCanvasCoordinates(this.game.wumpus.coordinate);
    this.context.font = "12px serif";
    this.context.fillText("W", c.x, c.y);
  }

  addGold() {
    const c = this.mapCoordinatesToCanvasCoordinates(this.game.treasure.coordinate);
    this.context.font = "12px serif";
    this.context.fillText("Gold", c.x, c.y);
  }

  addPits() {
    this.game.pits.forEach(p => {
      const c = this.mapCoordinatesToCanvasCoordinates(p.coordinate);
      this.context.font = "12px serif";
      this.context.fillText("Pit", c.x, c.y);
    });
  }

  addPlayers() {
    this.game.players.forEach(p => {
      const c = this.mapCoordinatesToCanvasCoordinates(p.coordinate);
      this.context.font = "12px serif";

      this.context.fillText(p.user + " " + p.direction, c.x, c.y);
    })
  }

  mapCoordinatesToCanvasCoordinates(c: Coordinate): Coordinate {
    const padding = 2;
    const { canvasHeight, boxWidth, boxHeight } = this.getCanvasProps();
    return {
      x: boxWidth * (c.x - 1) + padding,
      y: canvasHeight - (boxHeight * (c.y - 1)) - padding
    }
  }
}
