/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


var drowImageAndRect = function (startX, startY, width, height) {
    var c = document.getElementById("canvas"),
            ctx = c.getContext("2d");
    var image = new Image();
    image.src = '#{fileup.pictUrl}';
    c.width = image.width;
    c.height = image.height;

    image.onload = function () {
        ctx.drawImage(image, 0, 0);
    }
    ctx.beginPath();
    ctx.rect(parseInt(startX), parseInt(startY), parseInt(width), parseInt(height));

    ctx.lineWidth = 1;
    ctx.strokeStyle = 'white';
    ctx.stroke();
}
