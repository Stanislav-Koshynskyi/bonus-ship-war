const API_URL = 'http://localhost:8080/api';
const WS_URL = 'http://localhost:8080/ws';

let stompClient = null;
let playerId = null;
let gameSubscription = null;
let currentGame = null;
let myBoard = null;
let enemyBoard = null;
let currentMyCells = null;
let currentEnemyCells = null;


const registerScreen = document.getElementById('register-screen');
const lobbyScreen = document.getElementById('lobby-screen');
const placementScreen = document.getElementById('placement-screen');
const gameScreen = document.getElementById('game-screen');
const lobbyList = document.getElementById('games-list');
const nicknameInput = document.getElementById('nickname-input');
const registerBtn = document.getElementById('register-btn');
const updateLobbyBtn = document.getElementById('update-lobby');
const statusDiv = document.getElementById('status');
const createGameBtn = document.getElementById('create-game-btn');
const generateBtn = document.getElementById('generate-btn');
const readyBtn = document.getElementById('ready-btn');
const customTableSizes = {
    columns: Array.from({length: 12}, (_, i) => ({ idx: i, width: 25 }))
};

function showScreen(screen) {
    document.querySelectorAll('.screen').forEach(s => s.classList.add('hidden'));
    screen.classList.remove('hidden');
}

function updateStatus(message) {
    statusDiv.textContent = message;
}

registerBtn.addEventListener('click', async () => {
    const nickname = nicknameInput.value.trim();
    if (!nickname) {
        alert("Будь ласка, введіть нікнейм!");
        return;
    }

    try {
        registerBtn.disabled = true;
        updateStatus("Реєстрація...");

        const response = await fetch(`${API_URL}/player`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ nickName: nickname })
        });

        if (!response.ok) throw new Error("Помилка при реєстрації на сервері");

        const data = await response.json();
        
        playerId = data.id;

        updateStatus(`Вітаємо, ${nickname}! Підключення`);
        
        connectWebSocket();

    } catch (error) {
        console.error(error);
        alert("Не вдалося зареєструватись.");
        registerBtn.disabled = false;
        updateStatus("");
    }
});

function connectWebSocket() {
    const socket = new SockJS(WS_URL);
    stompClient = Stomp.over(socket);

    stompClient.connect({}, (frame) => {
        updateStatus('Підключено. Лобі.');
        
        showScreen(lobbyScreen);
        loadLobby();


        stompClient.subscribe(`/topic/player/${playerId}/board`, (message) => {
        const board = JSON.parse(message.body);
        renderMyBoard(board);
        });
        stompClient.subscribe(`/topic/player/${playerId}/enemy-board`, (message) => {
            const enemyBoardInput = JSON.parse(message.body);
            renderEnemyBoard(enemyBoardInput);
        });

        stompClient.subscribe(`/topic/player/${playerId}`, (message) => {
            currentGame = JSON.parse(message.body);
            subscribeToGame(currentGame.id);
            handleGameStateChange(currentGame);
        });

    }, (error) => {
        console.error('WebSocket Error: ', error);
        updateStatus(`Помилка з'єднання з сервером.`);
        registerBtn.disabled = false;
    });
}

function subscribeToGame(gameId){
    if (gameSubscription){
        gameSubscription.unsubscribe();
    }

    gameSubscription = stompClient.subscribe(`/topic/game/${gameId}`, (message) => {
        const payload = JSON.parse(message.body);
    
        if (payload.state) { 
            handleGameStateChange(payload); 
        } else {
            handleShotResult(payload);
        }
    });
}

createGameBtn.addEventListener('click', async() =>{
    stompClient.send(
        '/app/game/create',
        {},
        JSON.stringify({ playerId : playerId})
    );
});

function joinGame(gameIdToJoin) {
    subscribeToGame(gameIdToJoin);

    stompClient.send(
        '/app/game/join',
        {},
        JSON.stringify({ 
            gameId: gameIdToJoin, 
            playerId: playerId 
        })
    );
}

async function loadLobby() {
    try {
        const response = await fetch(`${API_URL}/game/waiting`);
        if (!response.ok) throw new Error("Помилка завантаження лобі");
        const games = await response.json();
        renderLobby(games);
    } catch (error) {
        console.error(error);
    }
}

function renderLobby(games) {
    lobbyList.innerHTML = '';
    
    if (games.length === 0) {
        lobbyList.innerHTML = '<p>Немає доступних ігор, створіть нову!</p>';
        return;
    }

    games.forEach(game => {
        const gameDiv = document.createElement('div');
        
        const info = document.createElement('span');
        info.textContent = `Гравець: ${game.player1.nickName} | Статус: ${game.state} `;
        
        const joinBtn = document.createElement('button');
        joinBtn.textContent = 'Приєднатися';
        joinBtn.onclick = () => joinGame(game.id);

        gameDiv.appendChild(info);
        gameDiv.appendChild(joinBtn);
        lobbyList.appendChild(gameDiv);
    });
}

function handleGameStateChange(game) {
    currentGame = game;
    if (game.state === 'WAITING_FOR_OPPONENT'){
        showScreen(placementScreen);
        updateStatus('Очікуємо приєднання опонента')
    }
    if (game.state === 'SHIP_PLACEMENT') {
        showScreen(placementScreen);
        updateStatus("Очікуємо розстановку кораблів...");
    } else if (game.state === 'IN_PROGRESS') {
        showScreen(gameScreen);
        updateStatus("Гра в процесі");
        if (currentMyCells) {
            renderMyBoard({ cells: currentMyCells });
        }
    } else if (game.state === 'FINISHED') {
        updateStatus(`Гра завершена! Переможець: ${game.winner ? game.winner.nickName : 'невідомо'}`);
    }
}

function handleShotResult(shotResult) {
    if (shotResult.result === 'WIN') {
        const winnerName = shotResult.winner ? shotResult.winner.nickName : "Невідомий";
        updateStatus(`Гра завершена! Переможець: ${winnerName}`);
        alert(`Гру завершено!\nПереможець: ${winnerName}`);
        currentGame.state = 'FINISHED'; 
        currentGame = null;
        if (gameSubscription) {
            gameSubscription.unsubscribe();
            gameSubscription = null;
        }
        if (myBoard) {
            myBoar.dispose();
            myBoard = null;
        }
        if (enemyBoard) {
            enemyBoard.dispose();
            enemyBoard = null;
        }
        showScreen(lobbyScreen);
        loadLobby();
        updateStatus("Ви повернулись у лобі. Оберіть або створіть нову гру.");
    }
}
updateLobbyBtn.addEventListener(('click'),async () =>{
    loadLobby();
})

function squereArrayToJson(cells){
    const data = [];
    for (let x = 0; x < cells.length; x++) {
        for (let y = 0; y < cells[x].length; y++) {
            data.push({
                "X": x.toString(),
                "Y": y.toString(),
                "State": cells[x][y]
            });
        }
    }
    return data;
}

function applyStyleByState(cellBuilder, state) {
    cellBuilder.text = "ㅤ"
    if (state === "SHIP") {
        cellBuilder.addClass("cell-ship");
    } else if (state === "VOID") {
        cellBuilder.addClass("cell-void");
    } else if (state === "MISS") {
        cellBuilder.addClass("cell-miss");
    } else if (state === "ATTACKED_SHIP_PART") {
        cellBuilder.addClass("cell-hit");
    } else if (state === "DEAD_SHIP") {
        cellBuilder.addClass("cell-dead");
    }
}
function styleMyCell(cellBuilder, cellData) {
    if (cellData.type === "value" && cellData.columns[0] && cellData.rows[0]) {
        const x = parseInt(cellData.columns[0].caption);
        const y = parseInt(cellData.rows[0].caption);
        const state = currentMyCells[x][y];
        applyStyleByState(cellBuilder, state);
    }
}
function styleEnemyCell(cellBuilder, cellData) {
    if (cellData.type === "value" && cellData.columns[0] && cellData.rows[0]) {
        const x = parseInt(cellData.columns[0].caption);
        const y = parseInt(cellData.rows[0].caption);
        const state = currentEnemyCells[x][y];
        applyStyleByState(cellBuilder, state);
    }
}

function renderMyBoard(board) {
    const data = squereArrayToJson(board.cells);
    currentMyCells = board.cells;
    const containerId = currentGame.state === 'SHIP_PLACEMENT' 
        ? "#my-board-placement" 
        : "#my-board-game";
    if (myBoard) {
        if (myBoard.container !== containerId) {
            myBoard.dispose();
            myBoard = null;
        } else {
            myBoard.updateData({ data: data });
            return;
        }
    }
    if (!myBoard) {
        myBoard = new WebDataRocks({
            container: containerId,
            width: 577,
            height: 391,
            toolbar: false,
            report: {
                dataSource: { data: data },
                slice: {
                    rows: [{ uniqueName: "Y" }],
                    columns: [{ uniqueName: "X" }],
                    measures: [{ uniqueName: "State" }]
                },
                options: {
                    grid: {
                        showTotals: "off",
                        showGrandTotals: "off",
                        showFilter: false,
                        showHierarchies: false
                    }
                },
                tableSizes: customTableSizes
            },
            customizeCell: styleMyCell
        });
    }
}

function renderEnemyBoard(enemyData) {
    const data = squereArrayToJson(enemyData.cells);
    currentEnemyCells= enemyData.cells;
    if (enemyBoard) {
        enemyBoard.updateData({ data: data });
    } else {
        enemyBoard = new WebDataRocks({
            container: "#enemy-board-game",
            width: 577,
            height: 391,
            toolbar: false,
            report: {
                dataSource: { data: data },
                slice: {
                    rows: [{ uniqueName: "Y" }],
                    columns: [{ uniqueName: "X" }],
                    measures: [{ uniqueName: "State" }]
                },
                options: {
                    grid: {
                        showTotals: "off",
                        showGrandTotals: "off",
                        showFilter: false,
                        showHierarchies: false
                    }
                },
                tableSizes: customTableSizes
            },
            customizeCell: styleEnemyCell
        });
        enemyBoard.on('cellclick', (cell) => {
            if (cell.type === "value" && cell.columns[0] && cell.rows[0]) {
                const x = parseInt(cell.columns[0].caption);
                const y = parseInt(cell.rows[0].caption);
                
                stompClient.send('/app/game/shot', {}, JSON.stringify({
                    gameId: currentGame.id,
                    playerId: playerId,
                    x: x,
                    y: y
                }));
            }
        });
    }
}

generateBtn.addEventListener('click', async () =>{
    stompClient.send('/app/game/board/generate', {}, JSON.stringify({
        gameId: currentGame.id,
        playerId: playerId
    }));
})
readyBtn.addEventListener('click', async () => {
    stompClient.send('/app/game/ready', {}, JSON.stringify({
        gameId: currentGame.id,
        playerId: playerId
    }));
})
