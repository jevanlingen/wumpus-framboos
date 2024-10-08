import { doGameAction, getCompetition, getCompetitionIds, getGame, getGameIds, register } from "./scripts/api.mjs";
import { aStar } from "./scripts/a-star.mjs";

// type Perception = 'LADDER' | 'STENCH' | 'BREEZE' | 'GLITTER' | 'BUMP' | 'SCREAM';
// const GAME_ACTIONS = ['enter', 'turn-left', 'turn-right', 'move-forward', 'grab', 'shoot', 'climb', 'restart'];
async function playGame(gameId) {

    const gameInfo = await getGame(gameId);

    //knowledge base
    const kb = {
        gridSize: gameInfo.gridSize,
        visited: [],
        pits: [],
        possiblePits: new Set([]),
        notPits: new Set(['1,1']),
        wumpus: undefined,
        wumpusDead: false,
        possibleWumpus: [],
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

        if (nextAction === 'enter' && currentField !== '1,1') {
            // logic can only work when starting from 1,1
            actions = ['restart'];
            continue;
        }
        const adjacentFields = getAdjacentFieldsForCoordinate(state.coordinate, kb.gridSize);

        // infer knowledge
        if (!kb.visited.includes(currentField)) {
            kb.visited.push(currentField);
            updateKnowledgeBaseBasedOnPerceptions(currentField, adjacentFields, state, kb);

            //OK fields are fields that have no wumpus and no pit
            kb.okFields = [];
            kb.notVisitedOKFields = [];
            kb.notWumpus.forEach(field => {
                if (kb.notPits.has(field)) {
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
                    nextActions = nextActions.concat(getActionsToGetToField(currentField, state.direction, kb.notVisitedOKFields[kb.notVisitedOKFields.length -1], kb.okFields));
                }
            }

            if (nextActions.length == 0) {
                console.log('No next actions found, but game not overrr.....');
                throw 'No next actions found, but game not overrr.....';
                // TODO maybe kill wumpus when known and move to wumpus field
            }

            actions.push(...nextActions);
        } else {
            if (kb.gold === currentField && !state.hasTreasure) {
                console.log('found gold', currentField);
                actions = ['grab'];
            }
        }
    }
}

function getActionsToGetToField(currentField, currentDirection, goalField, allowedFields) {
    const { path, actions } = aStar(currentField, goalField, currentDirection, allowedFields);
    console.log(currentField, goalField, currentDirection, path, actions);
    
    return actions;
}

function updateKnowledgeBaseBasedOnPerceptions(currentField, adjacentFields, state, knowledgeBase) {
    if (state.perceptions.includes('STENCH')) {
        updateWumpusRelatedKnowledge(adjacentFields, knowledgeBase);
    } else {
        adjacentFields
            .filter(a => !knowledgeBase.notWumpus.includes(a))
            .forEach(a => knowledgeBase.notWumpus.push(a));
    }

    if (state.perceptions.includes('BREEZE')) {
        updatePitRelatedKnowledge(adjacentFields, knowledgeBase);
    } else {
        adjacentFields.forEach(a => knowledgeBase.notPits.add(a));
    }

    if (state.perceptions.includes('GLITTER')) {
        knowledgeBase.gold = currentField;
    }
    if (state.perceptions.includes('SCREAM')) {
        knowledgeBase.wumpusDead = true;
    }
}

function updatePitRelatedKnowledge(adjacentFields, knowledgeBase) {
    if (adjacentFields.some(field => knowledgeBase.pits.includes(field))) {
        // pit already found. add other adjacent fields to notPits.
        adjacentFields
            .filter(a => !knowledgeBase.pits.includes(a))
            .forEach(a => {
                knowledgeBase.notPits.add(a);
                knowledgeBase.possiblePits.delete(a);
            });
    }


    const probableFieldsWithPits = adjacentFields.filter(field => !knowledgeBase.notPits.has(field));
    if (probableFieldsWithPits.length === 1 && !knowledgeBase.pits.includes(probableFieldsWithPits[0])) {
        knowledgeBase.pits.push(probableFieldsWithPits[0]);
    } else if (probableFieldsWithPits.length > 1) {
        let foundPit = false;

        if (knowledgeBase.possiblePits.size) {
            // If one of the adjacent fields is already marked as a possible pit-field, we deduce that it is a pit-field.
            for (const pfwp of probableFieldsWithPits) {
                console.log(pfwp, knowledgeBase.possiblePits, knowledgeBase.possiblePits.has(pfwp));

                if (knowledgeBase.possiblePits.has(pfwp)) {
                    knowledgeBase.pits.push(pfwp);
                    foundPit = true;
                    break;
                }
            }
        }
        if (foundPit) {
            // assuming that a breeze only relates exactly 1 adjacent field, we can conclude that the other adjacent fields are not pits.
            adjacentFields
                .filter(a => !knowledgeBase.pits.includes(a))
                .forEach(a => knowledgeBase.notPits.add(a));
        } else {
            // add all probableFieldsWithPits to possiblePits
            for (const pfwp of probableFieldsWithPits) {
                knowledgeBase.possiblePits.add(pfwp);
            }
        }
    }
}

function updateWumpusRelatedKnowledge(adjacentFields, knowledgeBase) {
    const probableFieldsWithWumpus = adjacentFields.filter(field => !knowledgeBase.notWumpus.includes(field));
    if (probableFieldsWithWumpus.length === 1) {
        knowledgeBase.wumpus = probableFieldsWithWumpus[0];
    } else {
        if (knowledgeBase.possibleWumpus.length) {
            for (const pfww of probableFieldsWithWumpus) {
                if (knowledgeBase.possibleWumpus.includes(pfww)) {
                    knowledgeBase.wumpus = pfww;
                    break;
                }
            }
        } else {
            knowledgeBase.possibleWumpus = probableFieldsWithWumpus;
        }
    }

    if (knowledgeBase.wumpus) {
        // the wumpus is found, add other adjacent fields to notWumpus list
        adjacentFields
            .filter(a => knowledgeBase.wumpus !== a && !knowledgeBase.notWumpus.includes(a))
            .forEach(a => knowledgeBase.notWumpus.push(a));

        // also add the possibleWumpus fields to the notWumpus list
        knowledgeBase.possibleWumpus
            .filter(a => knowledgeBase.wumpus !== a && !knowledgeBase.notWumpus.includes(a))
            .forEach(a => knowledgeBase.notWumpus.push(a));
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
