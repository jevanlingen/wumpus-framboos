import { doGameAction, getCompetition, getCompetitionIds, getGame, getGameIds, register } from "./scripts/api.mjs";


// type Perception = 'LADDER' | 'STENCH' | 'BREEZE' | 'GLITTER' | 'BUMP' | 'SCREAM';
// const GAME_ACTIONS = ['enter', 'turn-left', 'turn-right', 'move-forward', 'grab', 'shoot', 'climb', 'restart'];
async function playGame(gameId) {

    const gameInfo = await getGame(gameId);    
    // would be nice if you call start that it should restart at 1,1.

    //knowledge base
    const kb = {
        gridSize: gameInfo.gridSize,
        visited: [],
        pits: [],
        notPits: ['1,1'],
        wumpus: undefined,
        wumpusDead: false,
        notWumpus: ['1,1'],
        gold: undefined,
        okFields: [],
        notVisitedOKFields: []
    }

    let actions = ['enter'];

    while (actions.length) {
        const nextAction = actions.shift();
        let state = await doGameAction(gameId, nextAction);
        if (state.gameCompleted || state.death) {
            console.log('completed or death', state);
            return;
        }
        const currentField = `${state.coordinate.x},${state.coordinate.y}`;
        const adjacentFields = getAdjacentFieldsForCoordinate(state.coordinate, kb.gridSize);

        // infer knowledge
        if (!kb.visited.includes(currentField)) {
            kb.visited.push(currentField);
            updateKnowledgeBaseBasedOnPerceptions(currentField, adjacentFields, state, kb);

            //OK fields are fields that have no wumpus and no pit
            kb.okFields = [];
            kb.notVisitedOKFields = [];
            kb.notWumpus.forEach(field => {
                if (kb.notPits.includes(field)) {
                    // OK field
                    kb.okFields.push(field);
                    if (!kb.visited.includes(field)) {
                        kb.notVisitedOKFields.push(field);
                    }
                }
            });
        }

        if (actions.length == 0) {
            // determine next action(s)
            let nextActions = [];

            if (state.hasTreasure) {
                console.log('-------------------------------');
                console.log('HAS TREASURE, return to 1,1');
                console.log('-------------------------------');
                if (currentField === '1,1') {
                    nextActions.push('climb');
                } else {
                    nextActions = nextActions.concat(getActionsToGetToField(currentField, state.direction, '1,1', kb.okFields));
                }
            } else {
                if (kb.gold === currentField) {
                    console.log('found gold', currentField);
                    nextActions = ['grab'];
                } else {
                    console.log('next goal', `${currentField} -> ${kb.notVisitedOKFields[0]}`, kb.okFields, kb.notVisitedOKFields);
                    nextActions = nextActions.concat(getActionsToGetToField(currentField, state.direction, kb.notVisitedOKFields[0], kb.okFields));
                }
            }

            if (nextActions.length == 0) {
                console.log('No next actions found, but game not overrr.....');
                throw 'No next actions found, but game not overrr.....';
                // TODO maybe kill wumpus when known and move to wumpus field
            }

            actions.push(...nextActions);
        }
    }
}

function getActionsToGetToField(currentField, currentDirection, goalField, allowedFields, actions) {
    if (!actions) {
        actions = [];
    }
    if (currentField === goalField) {
        return actions;
    }

    const adjacentFields = getAdjacentFieldsWithDirection(currentField).filter(a => allowedFields.includes(a[0]));

    if (adjacentFields.length === 0) {
        return undefined;
    }

    const paths = [];
    for (let field of adjacentFields) {
        paths.push(getActionsToGetToField(
            field[0],
            field[1],
            goalField,
            allowedFields.filter(a => a != field[0]),
            [...actions, ...getTurnActions(currentDirection, field[1]), 'move-forward']
        ));
    }

    return paths.filter(p => !!p).sort()?.[0];
}

function getTurnActions(currentDirection, newDirection) {
    if (currentDirection === newDirection) {
        return [];
    } else if (
        (currentDirection === 'NORTH' && newDirection == 'SOUTH') ||
        (currentDirection === 'EAST' && newDirection == 'WEST') ||
        (currentDirection === 'SOUTH' && newDirection == 'NORTH') ||
        (currentDirection === 'WEST' && newDirection == 'EAST')
    ) {
        return ['turn-left', 'turn-left'];
    } else if (
        (currentDirection === 'NORTH' && newDirection == 'EAST') ||
        (currentDirection === 'EAST' && newDirection == 'SOUTH') ||
        (currentDirection === 'SOUTH' && newDirection == 'WEST') ||
        (currentDirection === 'WEST' && newDirection == 'NORTH')
    ) {
        return ['turn-right'];
    } else {
        return ['turn-left'];
    }
}

function getAdjacentFieldsWithDirection(currentField) {
    const [x, y] = currentField.split(',').map(i => parseInt(i));
    return [
        [`${x + 1},${y}`, 'EAST'], [`${x - 1},${y}`, 'WEST'], [`${x},${y + 1}`, 'NORTH'], [`${x},${y - 1}`, 'SOUTH']
    ];
}

function updateKnowledgeBaseBasedOnPerceptions(currentField, adjacentFields, state, knowledgeBase) {
    if (state.perceptions.includes('STENCH')) {
        const probableFieldsWithWumpus = adjacentFields.filter(field => !knowledgeBase.notWumpus.includes(field));
        if (probableFieldsWithWumpus.length === 1) {
            knowledgeBase.wumpus = probableFieldsWithWumpus[0];
        }
    } else {
        adjacentFields
            .filter(a => !knowledgeBase.notWumpus.includes(a))
            .forEach(a => knowledgeBase.notWumpus.push(a));
    }

    if (state.perceptions.includes('BREEZE')) {
        const probableFieldsWithPits = adjacentFields.filter(field => !knowledgeBase.notPits.includes(field));
        if (probableFieldsWithPits.length === 1 && !knowledgeBase.pits.includes(probableFieldsWithPits[0])) {
            knowledgeBase.pits.push(probableFieldsWithPits[0]);
        }
    } else {
        adjacentFields
            .filter(a => !knowledgeBase.notPits.includes(a))
            .forEach(a => knowledgeBase.notPits.push(a));
    }

    if (state.perceptions.includes('GLITTER')) {
        knowledgeBase.gold = currentField;
    }
    if (state.perceptions.includes('SCREAM')) {
        knowledgeBase.wumpusDead = true;
    }
}

function getAdjacentFieldsForCoordinate(coordinate, gridSize) {
    const adjacentFields = [];

    //north
    if (coordinate.y < gridSize) {
        adjacentFields.push(`${coordinate.x},${coordinate.y + 1}`);
    }
    //east
    if (coordinate.x < gridSize) {
        adjacentFields.push(`${coordinate.x + 1},${coordinate.y}`);
    }
    //south
    if (coordinate.y > 1) {
        adjacentFields.push(`${coordinate.x},${coordinate.y - 1}`);
    }
    //west
    if (coordinate.x > 1) {
        adjacentFields.push(`${coordinate.x - 1},${coordinate.y}`);
    }
    return adjacentFields;
}

async function start() {
    await register();
    await getGameIds();

    const competitionIds = await getCompetitionIds();

    for (let competitionId of competitionIds) {
        const competition = await getCompetition(competitionId);
        if (competition.currentGameId >= 0) {
            // play game
            console.log('play game ' + competition.currentGameId);
            await playGame(competition.currentGameId);
        }
    }
    throw 'bla';
}

async function retryStart() {
    try {
        await start();
    } catch (e) {
        console.error('Failed, restarting in 5 seconds', e);
        setTimeout(() => retryStart(), 5000);
    }
}
await retryStart();
