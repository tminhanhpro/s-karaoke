/* Ap dung co chu / tuong phan truoc khi ve giao dien (tranh nhay chu luc mo trang) */
(function () {
  try {
    var fs = Number(localStorage.getItem('k_fs')) || 1.2;
    document.documentElement.style.setProperty('--fs', String(fs));
    if (localStorage.getItem('k_ui') === 'classic') document.documentElement.setAttribute('data-ui', 'classic');
    var th = localStorage.getItem('k_theme');
    if (th === 'light' || th === 'dark') document.documentElement.setAttribute('data-theme', th);
    var hc = localStorage.getItem('k_hc');
    if (hc === null && window.matchMedia && matchMedia('(prefers-contrast: more)').matches) hc = '1';
    if (hc === '1') document.documentElement.setAttribute('data-hc', '1');
  } catch (e) {}
})();
