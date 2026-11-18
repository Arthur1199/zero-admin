(function () {
	var background = document.querySelector('.zero-job-background');
	if (!background) {
		return;
	}

	var bubbles = document.createDocumentFragment();
	for (var i = 0; i < 15; i++) {
		var bubble = document.createElement('span');
		var size = Math.random() * 80 + 40 + 'px';
		bubble.className = 'zero-job-bubble';
		bubble.style.width = size;
		bubble.style.height = size;
		bubble.style.left = Math.random() * 100 + '%';
		bubble.style.animationDuration = Math.random() * 10 + 15 + 's';
		bubble.style.animationDelay = Math.random() * 5 + 's';
		bubbles.appendChild(bubble);
	}
	background.appendChild(bubbles);
})();
