import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { Coordinate, Game, Player } from './../../model/game';
import { AvatarComponent } from "../avatar/avatar.component";
import { WumpusComponent } from "./wumpus/wumpus.component";
import { PitComponent } from "./pit/pit.component";
import { TreasureComponent } from "./treasure/treasure.component";

function sameCoordinate(c1: Coordinate, c2: Coordinate) {
  return c1.x === c2.x && c1.y === c2.y;
}

@Component({
  selector: 'app-game-grid',
  standalone: true,
  imports: [AvatarComponent, WumpusComponent, TreasureComponent, PitComponent],
  templateUrl: './game-grid.component.html',
  styleUrl: './game-grid.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class GameGridComponent {

  @Input({ required: true })
  set game(game: Game) {
    this._gridSize = game.gridSize;

    this._gridItems = Array(Math.pow(game.gridSize, 2))
      .fill(0)
      .map((_, i) => {
        const c: Coordinate = {
          x: i % game.gridSize + 1,
          y: game.gridSize - Math.floor(i / game.gridSize),
        }

        return {
          c,
          wumpus: sameCoordinate(game.wumpus.coordinate, c),
          treasure: sameCoordinate(game.treasure.coordinate, c),
          pit: game.pits.some(p => sameCoordinate(p.coordinate, c)),
          players: game.players.filter(p => sameCoordinate(p.coordinate, c)),
          id: `${c.x}-${c.y}`
        }
      });
  }
  @Input() highlightUserId: string | undefined = undefined;
  @Output() highlight = new EventEmitter<string | undefined>(); 

  _gridSize: number = 0;
  _gridItems: Array<any> = [];

  getPlayerRotation(player: Player): number {
    switch (player.direction) {
      case 'EAST':
        return 90;
      case 'NORTH':
        return 0;
      case 'SOUTH':
        return 180;
      case 'WEST':
        return 270;
    }
  }

}
