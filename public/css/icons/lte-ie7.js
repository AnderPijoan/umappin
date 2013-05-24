/* Load this script using conditional IE comments if you need to support IE 7 and IE 6. */

window.onload = function() {
	function addIcon(el, entity) {
		var html = el.innerHTML;
		el.innerHTML = '<span style="font-family: \'icomoon\'">' + entity + '</span>' + html;
	}
	var icons = {
			'icon-home' : '&#xe000;',
			'icon-newspaper' : '&#xe006;',
			'icon-bubbles' : '&#xe007;',
			'icon-cog' : '&#xe008;',
			'icon-heart-stroke' : '&#xe009;',
			'icon-trophy' : '&#xe00a;',
			'icon-magnifying-glass' : '&#xe00b;',
			'icon-key' : '&#xe00c;',
			'icon-users' : '&#xe001;',
			'icon-unlocked' : '&#xe002;',
			'icon-envelop' : '&#xe003;',
			'icon-switch' : '&#xe004;'
		},
		els = document.getElementsByTagName('*'),
		i, attr, html, c, el;
	for (i = 0; ; i += 1) {
		el = els[i];
		if(!el) {
			break;
		}
		attr = el.getAttribute('data-icon');
		if (attr) {
			addIcon(el, attr);
		}
		c = el.className;
		c = c.match(/icon-[^\s'"]+/);
		if (c && icons[c[0]]) {
			addIcon(el, icons[c[0]]);
		}
	}
};