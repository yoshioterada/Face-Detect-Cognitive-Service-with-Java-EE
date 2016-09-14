/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
if (window.addEventListener) { //for W3C DOM
    window.addEventListener("load", init, false);
} else if (window.attachEvent) { //for IE
    window.attachEvent("onload", init);
} else {
    window.onload = init;
}

function init() {
    //起動時は グループ部分は非表示
    //きどうじは グループぶぶんは非表示
    // hide the group section at start-up
    notShowData();
}

function notShowData() {
    document.getElementById("form:uploaded").style.display = "none";
}


function showData() {
    document.getElementById("form:uploaded").style.display = "block";
}

