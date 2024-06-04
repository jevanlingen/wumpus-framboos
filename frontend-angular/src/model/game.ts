export interface Game {
    id: number;
    gridSize: number;
    pits: Array<Pit>;
    wumpus: Wumpus;
    treasure: Treasure;
    players: Array<any>;
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