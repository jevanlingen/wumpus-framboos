import { User } from "./user";

export interface Game {
    id: number;
    name: string;
    gridSize: number;
    pits: Array<Pit>;
    wumpus: Wumpus;
    treasure: Treasure;
    players: Array<Player>;
    startingLocation: Coordinate;
}

export interface Pit {
    id: number;
    coordinate: Coordinate;
}

export interface Treasure {
    id: number;
    coordinate: Coordinate;
}

export interface Wumpus {
    id: number;
    coordinate: Coordinate;
}

export interface Coordinate {
    x: number;
    y: number;
}

export interface Player {
    id: number;
    user: User;
    direction: Direction;
    perceptions: Array<Perception>;
    coordinate: Coordinate;
    points: number;
    arrows: number;
    planks: number;
    wumpusAlive: boolean;
    hasTreasure: boolean;
    gameCompleted: boolean;
    death: boolean;
}

export type Direction = 'EAST' | 'NORTH' | 'SOUTH' | 'WEST';
export type Perception = 'LADDER' | 'STENCH' | 'BREEZE' | 'GLITTER' | 'BUMP' | 'SCREAM';
export const GAME_ACTIONS = ['enter', 'turn-left', 'turn-right', 'move-forward', 'grab', 'shoot', 'climb'] as const;
export type GameAction = typeof GAME_ACTIONS[number];
