class Node {
    constructor(coordinate, g, h, direction, parent = null, action = null) {
        this.coordinate = coordinate; // Coordinate as a string, e.g., '1,1'
        this.g = g;                   // Cost from the start node
        this.h = h;                   // Heuristic cost to the end node
        this.f = g + h;               // Total cost (f = g + h)
        this.direction = direction;   // Current direction (NORTH, EAST, SOUTH, WEST)
        this.parent = parent;         // Parent node for reconstructing the path
        this.action = action;         // Action taken to reach this node
    }
}

// Parse the coordinate string 'x,y' into [x, y] array
function parseCoordinate(coordinate) {
    return coordinate.split(',').map(Number);
}

// Calculate the Manhattan distance between two coordinates in 'x,y' format
function heuristic(coord1, coord2) {
    const [x1, y1] = parseCoordinate(coord1);
    const [x2, y2] = parseCoordinate(coord2);
    return Math.abs(x1 - x2) + Math.abs(y1 - y2);
}

// Check if a node is in a list
function isInList(list, coordinate, direction) {
    return list.some(node => node.coordinate === coordinate && node.direction === direction);
}

// Reconstruct the path from end node to start node, including the actions
function reconstructPath(endNode) {
    let path = [];
    let actions = [];
    let currentNode = endNode;
    while (currentNode) {
        path.push(currentNode.coordinate);
        if (currentNode.action) actions.push(currentNode.action);
        currentNode = currentNode.parent;
    }
    return { path: path.reverse(), actions: actions.reverse() }; // Reverse to get the path from start to end
}

// Get the new direction after turning
function getNewDirection(currentDirection, turn) {
    const directions = ['NORTH', 'EAST', 'SOUTH', 'WEST'];
    let currentIndex = directions.indexOf(currentDirection);
    if (turn === 'turn-left') {
        currentIndex = (currentIndex + 3) % 4; // Equivalent to -1 but keeps the index in range
    } else if (turn === 'turn-right') {
        currentIndex = (currentIndex + 1) % 4;
    }
    return directions[currentIndex];
}

// A* algorithm function with actions
export function aStar(start, end, currentDirection, coordinates) {
    const openList = [];
    const closedList = [];

    const startNode = new Node(start, 0, heuristic(start, end), currentDirection);
    openList.push(startNode);

    while (openList.length > 0) {
        openList.sort((a, b) => a.f - b.f);
        const currentNode = openList.shift();

        // Check if we've reached the end
        if (currentNode.coordinate === end) {
            return reconstructPath(currentNode);
        }

        closedList.push(currentNode);

        const directionOffsets = {
            'NORTH': [0, 1],   // Move up (increase y)
            'EAST': [1, 0],    // Move right (increase x)
            'SOUTH': [0, -1],  // Move down (decrease y)
            'WEST': [-1, 0]    // Move left (decrease x)
        };

        // Actions and their corresponding new directions
        const possibleActions = ['move-forward', 'turn-left', 'turn-right'];

        for (const action of possibleActions) {
            let [x, y] = parseCoordinate(currentNode.coordinate);
            let newX = x;
            let newY = y;
            let newDirection = currentNode.direction;

            if (action === 'move-forward') {
                newX += directionOffsets[currentNode.direction][0];
                newY += directionOffsets[currentNode.direction][1];
            } else {
                newDirection = getNewDirection(currentNode.direction, action);
            }

            const newCoordinate = `${newX},${newY}`;

            // Check if the new position is within bounds and is a valid coordinate
            if (coordinates.includes(newCoordinate) && !isInList(closedList, newCoordinate, newDirection)) {
                const g = currentNode.g + 1; // Increment cost by 1 for every action
                const h = heuristic(newCoordinate, end);
                const neighborNode = new Node(newCoordinate, g, h, newDirection, currentNode, action);

                const openNode = openList.find(node => node.coordinate === newCoordinate && node.direction === newDirection);
                if (!openNode) {
                    openList.push(neighborNode);
                } else if (g < openNode.g) {
                    openNode.g = g;
                    openNode.f = g + openNode.h;
                    openNode.parent = currentNode;
                    openNode.action = action;
                }
            }
        }
    }

    return { path: [], actions: [] }; // Return empty path and actions if no path is found
}

// Example usage:
// const coordinates = [
//     '0,0', '1,0', '2,0', '3,0',
//     '0,1',        '2,1', '3,1',
//     '0,2', '1,2', '2,2', '3,2'
// ];
// const start = '0,0';
// const end = '3,2';

// const result = aStar(start, end, 'NORTH', coordinates);
// console.log("Path:", result.path);
// console.log("Actions:", result.actions);
