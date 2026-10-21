board:clearBoard()

local function createEmptySpace()
    return tile:create(
        "EMPTY",
        0,
        "blumenstrasse.png",
        -1
    )
end

local function createBlumenstrasse(name, boardPos)
    return {
        pos = boardPos,
        text = name,
        price = 0,
        imageLink = "blumenstrasse.png",
        onEnter = function(player)
            print(tostring(player) .. " entered the tile " .. name)
        end
    }
end

local startTile = {
    pos = 0,
    text = "Start",
    price = 0,
    imageLink = "start.jpeg",
    onEnter = function(player)
        player:reduceMoney(-200)
        print(tostring(player) .. " received 200$ when crossing the start tile")
    end
}

local blumenstrasse1 = createBlumenstrasse("Blumenstrasse 13", 1)
local blumenstrasse2 = createBlumenstrasse("Blumenstrasse 13", 2)
local blumenstrasse3 = createBlumenstrasse("Blumenstrasse 13", 3)
local blumenstrasse4 = createBlumenstrasse("Blumenstrasse 13", 4)
local blumenstrasse5 = createBlumenstrasse("Blumenstrasse 13", 5)
local blumenstrasse6 = createBlumenstrasse("Blumenstrasse 13", 6)
local blumenstrasse7 = createBlumenstrasse("Blumenstrasse 13", 7)

local boardLayout = {}

for y = 1, 3 do
    boardLayout[y] = {}
    for x = 1, 3 do
        boardLayout[y][x] = createEmptySpace()
    end
end

boardLayout[3][3] = startTile
boardLayout[3][2] = blumenstrasse1
boardLayout[3][1] = blumenstrasse2
boardLayout[2][1] = blumenstrasse3
boardLayout[1][1] = blumenstrasse4
boardLayout[1][2] = blumenstrasse5
boardLayout[1][3] = blumenstrasse6
boardLayout[2][3] = blumenstrasse7

board:loadBoard(boardLayout)