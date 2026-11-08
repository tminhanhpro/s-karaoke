(function () {
  'use strict';

  // ------------------------------------------------------------ i18n
  var T = {
    en: {
      search_ph: 'Search song / paste YouTube link', tab_search: 'SEARCH', tab_queue: 'QUEUE',
      add: 'Add', added: 'Added: ', in_queue: 'In queue', play_next: 'Play next', play_now: 'Play now',
      prioritize: 'Move to top', remove: 'Remove', move_up: 'Move up', move_down: 'Move down', close: 'Close',
      now_playing: 'Now playing', next: 'Next: ', nothing: 'Nothing is waiting. The TV plays suggested songs.',
      no_results: 'No results', recent: 'Recent searches', loading: 'Loading…',
      paste_title: 'Add a YouTube link', paste_ph: 'Paste YouTube link or video ID', title_opt: 'Song title (optional)',
      cancel: 'Cancel', menu: 'Settings', language: 'Language', device: 'Your device name',
      karaoke_mode: 'Karaoke mode (append "karaoke" to searches)', reset_pairing: 'Reset pairing',
      reset_confirm: 'Disconnect all phones and change the QR code?', conn: 'Connection', about: 'About',
      reconnecting: 'Reconnecting…', lost: 'Connection lost. Make sure your phone is on the same Wi-Fi as the TV',
      unauth: 'Not connected. Scan the QR code on the TV again.',
      err_UNAVAILABLE: 'Video unavailable, skipped: ', err_PLAYBACK_FAILED: 'Could not play, skipped: ',
      err_generic: 'Something went wrong', dup_confirm: 'Already in queue. Add anyway?', yes: 'Add',
      skip_confirm: 'Someone else just changed the queue. Continue?', vol: 'Volume ',
      mic_hint: 'Tap the microphone key on your keyboard to dictate',
      mic_denied: 'Microphone access was blocked. Allow the microphone for this page in your browser, then try again.',
      mic_unsupported: 'This browser has no voice search. Use the microphone key on your keyboard.',
      no_speech: 'Did not hear anything. Tap the mic and speak again.',
      fallback_hint: 'Playing via fallback player, ads may appear',
      net_wait: 'The TV lost its network. Waiting…', halted: 'Several songs failed in a row. Pick a song to continue.',
      invalid_url: 'Paste a valid YouTube video link', source: 'Search source', yt_src: 'YouTube',
      volume_slider: 'TV volume', res_play: 'Video quality on TV', l_play: 'Play', l_pause: 'Pause', l_next: 'Next song', l_replay: 'Replay', l_mic: 'Mic', l_vdown: 'Quieter', l_vup: 'Louder', l_react: 'Cheer', l_link: 'Paste link', text_size: 'Text size', ui_mode: 'Interface style', ui_new: 'New (big, labeled)', ui_old: 'Classic (compact)', theme: 'Display mode', th_auto: 'Auto', th_light: 'Light', th_dark: 'Dark', fs_m: 'Normal', fs_l: 'Large', fs_xl: 'Extra large', hc_mode: 'High contrast', search_by: 'Search by', m_song: 'Song', m_channel: 'Channel', m_lyrics: 'Lyrics', genre: 'Genre', open_channel: 'Open channel (its songs)', yt_channel: 'Channel page on YouTube', filter_label: 'Filter', filter_clear: 'tap to clear', tv: 'TV', backend: 'Player', res: 'Max resolution', queue_full: 'Queue is full',
      donate_text: 'Enjoying the app? Buy me a coffee ☕ Thank you so much! 💙',
      listening: 'Listening…', search_failed: 'Search failed. Try again.',
      link_found: 'YouTube link detected', add_end: 'Add to queue', add_next: 'Play next', link_loading: 'Reading the link…',
      tab_favs: '♥ FAVORITES', fav: 'Favorite', fav_on: 'Saved', fav_add: 'Add to favorites', unfav: 'Remove from favorites',
      fav_added: 'Saved to favorites', fav_removed: 'Removed from favorites', undo: 'Undo',
      fav_empty: 'No favorite songs yet. Tap “♡ Favorite” on a song to keep it here (saved on this phone).',
      react_title: 'Reactions', react_hint: 'Tap to send to the TV screen', react_fail: 'Could not send. Check the connection.',
      msg_ph: 'Type a message for the TV screen…', msg_send: 'Send', msg_sent: 'Sent to the TV', msg_wait: 'Please wait a moment, then send again',
      q1: 'Great singing! 👏', q2: 'Happy birthday! 🎂', q3: 'Cheers! 🍻', q4: 'Your turn to sing! 🎤', q5: 'Love this song ❤️',
      q6: 'Absolutely fire! 🔥', q7: 'Golden voice! 🌟', q8: 'Keep singing! 🎶', q9: 'Better than the original! 😍', q10: 'One more song! 🙌', q11: 'Happy New Year! 🧧', q12: 'Congrats on the wedding! 💍', q13: 'Happy housewarming! 🏠', q14: 'Happy birthday to our elder! 🎂', q15: 'Cheers to good health! 🥂', q16: 'Bottoms up! 🍻', q17: 'Perfect score! 💯', q18: 'Goosebumps! 😳', q19: 'Play something upbeat! 💃', q20: 'Next song please! ⏭️', q21: 'Turn it up! 🔊', q22: 'A bit quieter please! 🔉', q23: 'Sing along everyone! 🎤', q24: 'Mic is free, who is next? 🎙️', q25: 'Thank you all! 🙏', q26: 'Love you all! 💖', q27: 'So much fun! 🥳', q28: 'Top of the top! 👑', q29: 'Hello everyone! 👋', q30: 'Have fun everyone! 🌈', q31: 'This song is mine! 😎', q32: 'Sing it again! 🔁', q33: 'Everyone sounds great! 💫', q34: 'Time to eat! 🍽️', q35: 'See you next song! 👋'
    },
    vi: {
      search_ph: 'Tìm bài hát / dán link YouTube', tab_search: 'TÌM KIẾM', tab_queue: 'DANH SÁCH',
      add: 'Thêm', added: 'Đã thêm: ', in_queue: 'Đã chọn', play_next: 'Phát tiếp theo', play_now: 'Phát ngay',
      prioritize: 'Đưa lên đầu', remove: 'Xóa', move_up: 'Lên', move_down: 'Xuống', close: 'Đóng',
      now_playing: 'Đang phát', next: 'Tiếp theo: ', nothing: 'Chưa có bài nào chờ. TV đang phát bài gợi ý.',
      no_results: 'Không có kết quả', recent: 'Tìm gần đây', loading: 'Đang tải…',
      paste_title: 'Thêm link YouTube', paste_ph: 'Dán link YouTube hoặc mã video', title_opt: 'Tên bài (không bắt buộc)',
      cancel: 'Hủy', menu: 'Cài đặt', language: 'Ngôn ngữ', device: 'Tên điện thoại của bạn',
      karaoke_mode: 'Chế độ karaoke (tự thêm chữ "karaoke" khi tìm)', reset_pairing: 'Đặt lại ghép nối',
      reset_confirm: 'Ngắt tất cả điện thoại và đổi mã QR?', conn: 'Kết nối', about: 'Giới thiệu',
      reconnecting: 'Đang kết nối lại…', lost: 'Mất kết nối. Hãy chắc chắn điện thoại dùng cùng Wi-Fi với TV',
      unauth: 'Chưa kết nối. Hãy quét lại mã QR trên TV.',
      err_UNAVAILABLE: 'Video không phát được, đã bỏ qua: ', err_PLAYBACK_FAILED: 'Không phát được, đã bỏ qua: ',
      err_generic: 'Có lỗi xảy ra', dup_confirm: 'Bài này đã có trong danh sách. Vẫn thêm?', yes: 'Thêm',
      skip_confirm: 'Người khác vừa thay đổi danh sách. Tiếp tục?', vol: 'Âm lượng ',
      mic_hint: 'Bấm vào biểu tượng micro trên bàn phím để nói',
      mic_denied: 'Chưa được phép dùng micro. Hãy cho phép micro cho trang này trong trình duyệt rồi thử lại.',
      mic_unsupported: 'Trình duyệt này không hỗ trợ tìm bằng giọng nói. Hãy bấm micro trên bàn phím.',
      no_speech: 'Không nghe thấy gì. Bấm micro và nói lại nhé.',
      fallback_hint: 'Đang phát bằng trình phát dự phòng, có thể có quảng cáo',
      net_wait: 'TV mất mạng. Đang chờ…', halted: 'Nhiều bài liên tiếp bị lỗi. Hãy chọn bài khác để tiếp tục.',
      invalid_url: 'Hãy dán link video YouTube hợp lệ', source: 'Nguồn tìm kiếm', yt_src: 'YouTube',
      volume_slider: 'Âm lượng TV', res_play: 'Chất lượng video trên TV', l_play: 'Phát', l_pause: 'Tạm dừng', l_next: 'Qua bài', l_replay: 'Hát lại', l_mic: 'Mic', l_vdown: 'Nhỏ tiếng', l_vup: 'To tiếng', l_react: 'Cổ vũ', l_link: 'Dán link', text_size: 'Cỡ chữ', ui_mode: 'Kiểu giao diện', ui_new: 'Mới (nút to, có chữ)', ui_old: 'Cũ (gọn)', theme: 'Chế độ hiển thị', th_auto: 'Tự động', th_light: 'Sáng', th_dark: 'Tối', fs_m: 'Vừa', fs_l: 'Lớn', fs_xl: 'Rất lớn', hc_mode: 'Chữ đậm, tương phản cao', search_by: 'Tìm theo', m_song: 'Tên bài', m_channel: 'Kênh', m_lyrics: 'Lời bài hát', genre: 'Thể loại', open_channel: 'Mở kênh (xem bài của kênh)', yt_channel: 'Trang YouTube của kênh', filter_label: 'Đang lọc', filter_clear: 'bấm để bỏ lọc', tv: 'TV', backend: 'Trình phát', res: 'Độ phân giải tối đa', queue_full: 'Danh sách đã đầy',
      donate_text: 'Thấy app vui thì mời mình ly cà phê nhé ☕ Cảm ơn bạn nhiều lắm! 💙',
      listening: 'Đang nghe…', search_failed: 'Tìm kiếm lỗi. Thử lại nhé.',
      link_found: 'Đã nhận link YouTube', add_end: 'Thêm vào danh sách', add_next: 'Phát tiếp theo', link_loading: 'Đang đọc link…',
      tab_favs: '♥ YÊU THÍCH', fav: 'Yêu thích', fav_on: 'Đã thích', fav_add: 'Thêm vào yêu thích', unfav: 'Bỏ yêu thích',
      fav_added: 'Đã thêm vào yêu thích', fav_removed: 'Đã bỏ khỏi yêu thích', undo: 'Hoàn tác',
      fav_empty: 'Chưa có bài yêu thích. Bấm “♡ Yêu thích” ở bài hát để lưu lại đây (lưu trên điện thoại này).',
      react_title: 'Bộ sưu tập', react_hint: 'Bấm để gửi hiệu ứng lên màn hình TV', react_fail: 'Gửi không được. Kiểm tra kết nối nhé.',
      msg_ph: 'Nhắn tin lên màn hình TV…', msg_send: 'Gửi', msg_sent: 'Đã gửi lên TV', msg_wait: 'Chờ một chút rồi gửi tiếp nhé',
      q1: 'Hát hay quá! 👏', q2: 'Chúc mừng sinh nhật! 🎂', q3: 'Cạn ly! 🍻', q4: 'Mời bạn lên hát! 🎤', q5: 'Bài này hay lắm ❤️',
      q6: 'Quá đỉnh! 🔥', q7: 'Giọng ca vàng! 🌟', q8: 'Hát tiếp đi! 🎶', q9: 'Hay hơn ca sĩ luôn! 😍', q10: 'Một bài nữa nha! 🙌', q11: 'Chúc mừng năm mới! 🧧', q12: 'Chúc mừng đám cưới! 💍', q13: 'Chúc mừng tân gia! 🏠', q14: 'Mừng thọ ông bà! 🎂', q15: 'Chúc sức khỏe cả nhà! 🥂', q16: 'Dzô dzô dzô! 🍻', q17: '100% luôn! 💯', q18: 'Nổi da gà luôn! 😳', q19: 'Bật bài vui vui đi! 💃', q20: 'Đổi bài đi nha! ⏭️', q21: 'Tăng âm lượng lên! 🔊', q22: 'Nhỏ tiếng lại chút! 🔉', q23: 'Hát chung nào mọi người! 🎤', q24: 'Mic đây, ai hát tiếp? 🎙️', q25: 'Cảm ơn mọi người! 🙏', q26: 'Yêu cả nhà! 💖', q27: 'Vui quá trời! 🥳', q28: 'Đỉnh của chóp! 👑', q29: 'Xin chào cả nhà! 👋', q30: 'Chúc cả nhà vui vẻ! 🌈', q31: 'Bài này của mình nha! 😎', q32: 'Hát lại lần nữa! 🔁', q33: 'Ai cũng hát hay hết! 💫', q34: 'Mời cả nhà ăn nào! 🍽️', q35: 'Hẹn bài sau nha! 👋'
    }
  };

  function pref(k, v) {
    try {
      if (v === undefined) return localStorage.getItem('k_' + k);
      localStorage.setItem('k_' + k, v);
    } catch (e) { return null; }
    return null;
  }

  var lang = pref('lang') || (((navigator.language || 'en').slice(0, 2) === 'vi') ? 'vi' : 'en');
  function t(k) { return (T[lang] && T[lang][k]) || T.en[k] || k; }

  var qs = new URLSearchParams(location.search);
  var token = qs.get('token');
  if (token) { try { history.replaceState(null, '', location.pathname); } catch (e) {} }

  var device = pref('device') || ('Phone ' + Math.random().toString(16).slice(2, 6).toUpperCase());
  pref('device', device);
  var karaoke = pref('karaoke') !== '0';

  // ------------------------------------------------------------ icons (simple line icons)
  function svg(inner) { return '<svg viewBox="0 0 24 24" aria-hidden="true">' + inner + '</svg>'; }
  var IC = {
    play: svg('<path d="M7 4.5v15l12-7.5z"/>'),
    pause: svg('<path d="M8 5v14M16 5v14" stroke-width="3"/>'),
    next: svg('<path d="M5 5l10 7-10 7z"/><path d="M19 5v14"/>'),
    replay: svg('<path d="M4.5 12a7.5 7.5 0 1 0 2.3-5.4"/><path d="M4 4v4.8h4.8"/>'),
    mic: svg('<rect x="9" y="3" width="6" height="11" rx="3"/><path d="M5 11a7 7 0 0 0 14 0M12 18v3"/>'),
    vdown: svg('<path d="M4 9v6h4l5 4V5L8 9z"/><path d="M16.5 9.5a4 4 0 0 1 0 5"/>'),
    vup: svg('<path d="M4 9v6h4l5 4V5L8 9z"/><path d="M16.5 9.5a4 4 0 0 1 0 5M19 6.5a8 8 0 0 1 0 11"/>'),
    queue: svg('<path d="M4 6h10M4 11h10M4 16h6"/><path d="M18 16V6.5l3-.8"/><circle cx="16" cy="16.5" r="2"/>'),
    react: svg('<circle cx="12" cy="12" r="9"/><path d="M8.3 14c.9 1.7 2.2 2.5 3.7 2.5s2.8-.8 3.7-2.5"/><path d="M9 9.6v.6M15 9.6v.6" stroke-width="2.6"/>'),
    heart: svg('<path d="M12 20.8s-7.6-4.7-9.3-9.5C1.5 7.9 3.6 4.4 7.2 4.4c1.9 0 3.5 1 4.8 2.9 1.3-1.9 2.9-2.9 4.8-2.9 3.6 0 5.7 3.5 4.5 6.9-1.7 4.8-9.3 9.5-9.3 9.5z"/>'),
    link: svg('<path d="M10 14a4 4 0 0 0 5.7 0l3-3a4 4 0 0 0-5.7-5.7l-1 1"/><path d="M14 10a4 4 0 0 0-5.7 0l-3 3a4 4 0 0 0 5.7 5.7l1-1"/>')
  };
  function setIcon(id, name) {
    var b = $(id);
    var tg = b.querySelector('.ic') || b;
    if (b.dataset.ic !== name) { b.dataset.ic = name; tg.innerHTML = IC[name]; }
  }

  // ------------------------------------------------------------ helpers
  function $(id) { return document.getElementById(id); }
  function el(tag, cls, text) {
    var e = document.createElement(tag);
    if (cls) e.className = cls;
    if (text !== undefined && text !== null) e.textContent = text;
    return e;
  }
  function fmt(sec) {
    sec = Math.max(0, Math.round(sec || 0));
    var m = Math.floor(sec / 60), s = sec % 60;
    return m + ':' + (s < 10 ? '0' : '') + s;
  }
  function uuid() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
      var r = Math.random() * 16 | 0; return (c === 'x' ? r : (r & 3 | 8)).toString(16);
    });
  }
  function say(msg) { $('live').textContent = msg; }

  var toastTimer = null;
  function toast(msg, actionLabel, action, kind) {
    var box = $('toast');
    box.className = 'toast' + (kind === 'ok' ? ' ok' : '');
    box.textContent = '';
    var tx = el('div', 'toast-text', (kind === 'ok' ? '✓ ' : '') + msg);
    box.appendChild(tx);
    if (actionLabel) {
      var b = el('button', null, actionLabel);
      b.addEventListener('click', function () { box.hidden = true; action(); });
      box.appendChild(b);
    }
    box.hidden = false;
    say(msg);
    clearTimeout(toastTimer);
    toastTimer = setTimeout(function () { box.hidden = true; }, actionLabel ? 8000 : 4000);
  }
  var pillTimer = null;
  function pill(msg) {
    var p = $('pill'); p.textContent = msg; p.hidden = false;
    clearTimeout(pillTimer); pillTimer = setTimeout(function () { p.hidden = true; }, 1200);
  }
  function setBanner(msg) {
    var b = $('banner');
    if (msg) { b.textContent = msg; b.hidden = false; } else { b.hidden = true; }
  }

  // ------------------------------------------------------------ api
  function api(method, path, body, extra) {
    var h = { 'X-Device': encodeURIComponent(device), 'X-Remote-Heartbeat': '1' };
    if (token) h['Authorization'] = 'Bearer ' + token;
    var o = { method: method, headers: h, credentials: 'same-origin', cache: 'no-store' };
    if (method === 'POST') { h['Content-Type'] = 'application/json; charset=utf-8'; o.body = JSON.stringify(body || {}); }
    if (extra) { for (var k in extra) { h[k] = extra[k]; } }
    return fetch(path, o).then(function (r) {
      return r.json().catch(function () { return {}; }).then(function (j) {
        if (!r.ok) {
          var e = new Error((j.error && j.error.message) || 'error');
          e.code = j.error && j.error.code; e.status = r.status; throw e;
        }
        return j;
      });
    });
  }
  function errText(e) {
    if (e && e.code === 'INVALID_URL') return t('invalid_url');
    if (e && e.code === 'QUEUE_FULL') return t('queue_full');
    if (e && e.code === 'UPSTREAM_FAILED') return t('search_failed') + ' (' + (e.message || '') + ')';
    return (e && e.message) || t('err_generic');
  }

  // ------------------------------------------------------------ state
  var S = null;           // last snapshot
  var lastVersion = -1;
  var lastErrSeq = null;
  var lastQueueSig = '';
  var fails = 0;

  function inQueueSet() {
    var s = {};
    if (S) {
      S.queue.forEach(function (i) { s[i.videoId] = true; });
      if (S.nowPlaying && S.nowPlaying.source === 'USER') s[S.nowPlaying.videoId] = true;
    }
    return s;
  }

  var seeking = false, seekTimer = null;
  function setPlayLbl() {
    var l = $('b-play').querySelector('.lbl');
    if (l) l.textContent = t($('b-play').dataset.ic === 'pause' ? 'l_pause' : 'l_play');
  }
  function applyState(s) {
    var first = (S === null);
    S = s;
    // counter
    var n = s.queue.length;
    var tq = $('tab-queue');
    var newText = t('tab_queue') + ' (' + n + ')';
    if (tq.textContent !== newText) {
      var grew = !first && tq.dataset.n !== undefined && n > Number(tq.dataset.n);
      tq.textContent = newText; tq.dataset.n = String(n);
      if (grew) { tq.classList.remove('bump'); void tq.offsetWidth; tq.classList.add('bump'); }
    }
    // errors from the TV
    if (s.error && lastErrSeq !== null && s.error.seq !== lastErrSeq) {
      toast((t('err_' + s.error.code) || t('err_generic')) + (s.error.message || ''));
    }
    lastErrSeq = s.error ? s.error.seq : 0;

    // banners
    if (s.netWait) setBanner(t('net_wait'));
    else if (s.halted) setBanner(t('halted'));
    else if (s.fallbackHint) setBanner(t('fallback_hint'));
    else setBanner(null);

    // transport
    setIcon('b-play', (s.state === 'PLAYING' || s.state === 'RESOLVING') ? 'pause' : 'play');
    setPlayLbl();

    // mini now playing
    var np = s.nowPlaying;
    var mini = $('mini');
    if (np) {
      mini.hidden = false;
      $('mini-title').textContent = np.title;
      var rg = $('mini-range');
      rg.disabled = !(s.durationMs > 0);
      $('mini-dur').textContent = fmt(s.durationMs / 1000);
      if (!seeking) {
        rg.value = s.durationMs > 0 ? Math.min(1000, Math.round(s.positionMs * 1000 / s.durationMs)) : 0;
        $('mini-pos').textContent = fmt(s.positionMs / 1000);
      }
    } else { mini.hidden = true; }

    renderQueue();
    refreshResultBadges();
    $('d-vol').value = s.volume;
    if (s.settings && document.activeElement !== $('d-res')) $('d-res').value = String(s.settings.maxHeight);
  }

  function renderQueue() {
    if (!S) return;
    var np = S.nowPlaying;
    var nl = $('next-line');
    nl.textContent = S.nextUp ? (t('next') + S.nextUp.title) : '';

    // now card (updated every poll: progress)
    var card = $('now-card');
    if (np) {
      var sig = np.itemId;
      if (card.dataset.id !== sig) {
        card.dataset.id = sig;
        card.textContent = '';
        var r = el('div', 'row-item'); r.style.border = '0'; r.style.margin = '0'; r.style.padding = '0';
        r.appendChild(thumbBox(np));
        var col = el('div', 'info-col');
        col.appendChild(el('div', 'sub', t('now_playing')));
        col.appendChild(el('div', 'title', np.title));
        col.appendChild(el('div', 'sub', np.channel + (np.source === 'SEED' ? ' · ' + (lang === 'vi' ? 'gợi ý' : 'suggested') : '')));
        r.appendChild(col);
        var nmore = el('button', 'act more', '⋮'); nmore.setAttribute('aria-label', 'Menu');
        nmore.addEventListener('click', function () { openNowMenu(np); });
        r.appendChild(actsBox([nmore, favBtn(np)]));
        card.appendChild(r);
        var bar = el('div', 'bar'); var fill = el('div', 'fill'); fill.id = 'now-fill'; bar.appendChild(fill);
        card.appendChild(bar);
        var tm = el('div', 'times'); var a = el('span', null, '0:00'); a.id = 'now-pos';
        var b = el('span', null, '0:00'); b.id = 'now-dur'; tm.appendChild(a); tm.appendChild(b);
        card.appendChild(tm);
      }
      card.hidden = false;
      $('now-fill').style.width = (S.durationMs > 0 ? Math.min(100, S.positionMs * 100 / S.durationMs) : 0) + '%';
      $('now-pos').textContent = fmt(S.positionMs / 1000);
      $('now-dur').textContent = fmt(S.durationMs / 1000);
    } else { card.hidden = true; card.dataset.id = ''; }

    // list only when changed
    var qsig = S.queueVersion + '|' + S.queue.map(function (i) { return i.itemId + i.title; }).join(',');
    if (qsig === lastQueueSig) return;
    lastQueueSig = qsig;
    var ul = $('queue-list');
    ul.textContent = '';
    S.queue.forEach(function (it, idx) {
      var li = el('li', 'row-item');
      li.appendChild(el('div', 'pos', String(idx + 1)));
      li.appendChild(thumbBox(it));
      var col = el('div', 'info-col');
      col.appendChild(el('div', 'title', it.title));
      col.appendChild(el('div', 'sub', it.channel));
      if (it.addedBy) col.appendChild(el('div', 'sub', '👤 ' + it.addedBy));
      li.appendChild(col);
      var more = el('button', 'act more', '⋮'); more.setAttribute('aria-label', 'Menu');
      more.addEventListener('click', function () { openQueueMenu(it, idx); });
      li.appendChild(actsBox([more, favBtn(it)]));
      ul.appendChild(li);
    });
    $('queue-empty').textContent = S.queue.length ? '' : t('nothing');
  }

  // cum nut hanh dong sat le phai (xep doc): +, ba cham, trai tim
  function actsBox(btns) {
    var box = el('div', 'acts');
    btns.forEach(function (b) { box.appendChild(b); });
    return box;
  }

  function thumbBox(it) {
    var box = el('div', 'thumb');
    var img = document.createElement('img');
    img.loading = 'lazy'; img.decoding = 'async'; img.width = 320; img.height = 180; img.alt = '';
    img.addEventListener('error', function () {
      img.remove(); box.appendChild(el('div', 'noimg', '🎵'));
    });
    img.src = it.thumbnailUrl;
    box.appendChild(img);
    if (it.durationSec > 0) box.appendChild(el('span', 'dur', fmt(it.durationSec)));
    return box;
  }

  // ------------------------------------------------------------ polling
  function poll() {
    api('GET', '/api/state').then(function (s) {
      if (fails >= 3) { setBanner(null); }
      fails = 0;
      applyState(s.state ? s.state : s);
    }).catch(function (e) {
      fails++;
      if (e.status === 403) setBanner(t('unauth'));
      else if (fails >= 3) setBanner(t('lost'));
      else setBanner(t('reconnecting'));
    }).then(function () {
      setTimeout(poll, fails ? 2000 : 1000);
    });
  }

  // ------------------------------------------------------------ commands
  function command(method, path, body, okMsg) {
    return api(method, path, body).then(function (r) {
      if (r.state) applyState(r.state);
      if (okMsg) toast(okMsg);
      return r;
    }).catch(function (e) {
      if (e.status === 404) { toast(t('err_generic')); poll_once(); }
      else toast(errText(e));
    });
  }
  function poll_once() {
    api('GET', '/api/state').then(function (s) { applyState(s.state); }).catch(function () {});
  }
  function recentlyChangedByOther() {
    return S && S.lastChange && S.lastChange.by && S.lastChange.by !== device && S.lastChange.agoMs < 5000;
  }
  function confirmIfNeeded(fn) {
    if (recentlyChangedByOther()) toast(t('skip_confirm'), t('yes'), fn); else fn();
  }

  function addSong(it, position, force) {
    var body = { videoId: it.videoId, title: it.title, channel: it.channel, durationSec: it.durationSec,
                 position: position || 'end', addedBy: device, force: !!force };
    return api('POST', '/api/queue', body, { 'Idempotency-Key': uuid() }).then(function (r) {
      if (r.state) applyState(r.state);
      toast(t('added') + it.title, null, null, 'ok');
      refreshResultBadges();
    }).catch(function (e) {
      if (e.code === 'DUPLICATE_PENDING') toast(t('dup_confirm'), t('yes'), function () { addSong(it, position, true); });
      else toast(errText(e));
    });
  }

  // ------------------------------------------------------------ favorites (luu tren chinh dien thoai remote)
  var favs = loadFavs();
  function loadFavs() {
    try { var a = JSON.parse(pref('favs') || '[]'); return Array.isArray(a) ? a : []; } catch (e) { return []; }
  }
  function saveFavs() { pref('favs', JSON.stringify(favs)); }
  function favIndex(id) {
    for (var i = 0; i < favs.length; i++) { if (favs[i].videoId === id) return i; }
    return -1;
  }
  function isFav(id) { return favIndex(id) >= 0; }
  function favItem(f) {
    return { videoId: f.videoId, title: f.title, channel: f.channel, durationSec: f.durationSec || 0,
             thumbnailUrl: 'https://i.ytimg.com/vi/' + f.videoId + '/mqdefault.jpg' };
  }
  function toggleFav(it) {
    var id = it.videoId, i = favIndex(id);
    if (i >= 0) {
      var removed = favs.splice(i, 1)[0];
      saveFavs(); syncFavs();
      toast(t('fav_removed'), t('undo'), function () {
        if (favIndex(id) < 0) { favs.splice(Math.min(i, favs.length), 0, removed); saveFavs(); syncFavs(); }
      });
    } else {
      favs.unshift({ videoId: id, title: it.title || '', channel: it.channel || '', durationSec: it.durationSec || 0 });
      if (favs.length > 300) favs.length = 300;
      saveFavs(); syncFavs();
      pill('♥ ' + t('fav_added'));
    }
  }
  function paintFav(b) {
    var on = isFav(b.dataset.vid);
    b.classList.toggle('on', on);
    if (!b.firstChild) b.innerHTML = IC.heart;
    var lbl = on ? t('unfav') : t('fav_add');
    b.setAttribute('aria-label', lbl); b.title = lbl;
    b.setAttribute('aria-pressed', on ? 'true' : 'false');
  }
  function favBtn(it) {
    var b = el('button', 'favbtn'); b.type = 'button'; b.dataset.vid = it.videoId;
    b.addEventListener('click', function (ev) { ev.stopPropagation(); toggleFav(it); });
    paintFav(b);
    return b;
  }
  function syncFavs() {
    var bs = document.querySelectorAll('.favbtn');
    for (var i = 0; i < bs.length; i++) paintFav(bs[i]);
    if (!$('view-favs').hidden) renderFavs();
  }
  function favEntry(it) {
    var on = isFav(it.videoId);
    return { label: (on ? '♥ ' + t('unfav') : '♡ ' + t('fav_add')), bad: on, run: function () { toggleFav(it); } };
  }
  function renderFavs() {
    var ul = $('fav-list'); ul.textContent = '';
    favs.forEach(function (f) { ul.appendChild(songRow(favItem(f), true)); });
    $('fav-empty').textContent = favs.length ? '' : t('fav_empty');
    refreshResultBadges();
  }

  // ------------------------------------------------------------ reactions (bo suu tap: tha tim, vo tay, phao giay...)
  var REACTIONS = [
    { id: 'heart', e: '❤️', vi: 'Thả tim', en: 'Hearts' },
    { id: 'clap', e: '👏', vi: 'Vỗ tay', en: 'Applause' },
    { id: 'party', e: '🎉', vi: 'Pháo giấy', en: 'Confetti' },
    { id: 'fire', e: '🔥', vi: 'Lửa cháy', en: 'On fire' },
    { id: 'star', e: '⭐', vi: 'Ngôi sao', en: 'Stars' },
    { id: 'laugh', e: '😂', vi: 'Cười xỉu', en: 'LOL' },
    { id: 'love', e: '😍', vi: 'Mê mẩn', en: 'Love it' },
    { id: 'mic', e: '🎤', vi: 'Hát hay', en: 'Great voice' },
    { id: 'rose', e: '🌹', vi: 'Tặng hoa', en: 'Roses' },
    { id: 'firework', e: '🎆', vi: 'Pháo hoa', en: 'Fireworks' },
    { id: 'like', e: '👍', vi: 'Tuyệt vời', en: 'Awesome' },
    { id: 'beer', e: '🍺', vi: 'Cụng ly', en: 'Cheers' },
    { id: 'crown', e: '👑', vi: 'Vua hát', en: 'Top singer' },
    { id: 'dance', e: '💃', vi: 'Nhảy đi', en: 'Dance' },
    { id: 'balloon', e: '🎈', vi: 'Bóng bay', en: 'Balloons' },
    { id: 'cry', e: '😭', vi: 'Cảm động', en: 'Touched' },
    // ---- 20 hieu ung moi ----
    { id: 'rainbow', e: '🌈', vi: 'Cầu vồng', en: 'Rainbow' },
    { id: 'unicorn', e: '🦄', vi: 'Kỳ lân', en: 'Unicorn' },
    { id: 'rocket', e: '🚀', vi: 'Tên lửa', en: 'Rocket' },
    { id: 'lightning', e: '⚡', vi: 'Sấm sét', en: 'Thunder' },
    { id: 'snow', e: '❄️', vi: 'Tuyết rơi', en: 'Snowfall' },
    { id: 'sakura', e: '🌸', vi: 'Anh đào', en: 'Sakura' },
    { id: 'butterfly', e: '🦋', vi: 'Bướm bay', en: 'Butterflies' },
    { id: 'diamond', e: '💎', vi: 'Kim cương', en: 'Diamond' },
    { id: 'trophy', e: '🏆', vi: 'Cúp vàng', en: 'Trophy' },
    { id: 'champagne', e: '🍾', vi: 'Khui rượu', en: 'Champagne' },
    { id: 'cheers', e: '🥂', vi: 'Chúc mừng', en: 'Congrats' },
    { id: 'guitar', e: '🎸', vi: 'Guitar', en: 'Guitar' },
    { id: 'drum', e: '🥁', vi: 'Trống', en: 'Drums' },
    { id: 'cool', e: '😎', vi: 'Ngầu lòi', en: 'Too cool' },
    { id: 'partyface', e: '🥳', vi: 'Quẩy lên', en: 'Party' },
    { id: 'wow', e: '🤩', vi: 'Choáng!', en: 'Wow!' },
    { id: 'kiss', e: '😘', vi: 'Hôn gió', en: 'Kisses' },
    { id: 'ghost', e: '👻', vi: 'Hú hồn', en: 'Boo!' },
    { id: 'cake', e: '🎂', vi: 'Sinh nhật', en: 'Birthday' },
    { id: 'money', e: '🤑', vi: 'Phát tài', en: 'Jackpot' },
    // ---- 20 hieu ung moi (dot 2) ----
    { id: 'meteor', e: '☄️', vi: 'Sao băng', en: 'Meteors' },
    { id: 'sun', e: '☀️', vi: 'Mặt trời', en: 'Sunshine' },
    { id: 'moon', e: '🌙', vi: 'Đêm sao', en: 'Starry night' },
    { id: 'whale', e: '🐳', vi: 'Cá voi', en: 'Whale' },
    { id: 'dolphin', e: '🐬', vi: 'Cá heo', en: 'Dolphins' },
    { id: 'pizza', e: '🍕', vi: 'Mưa pizza', en: 'Pizza rain' },
    { id: 'icecream', e: '🍦', vi: 'Kem mát', en: 'Ice cream' },
    { id: 'popcorn', e: '🍿', vi: 'Bắp rang', en: 'Popcorn' },
    { id: 'gift', e: '🎁', vi: 'Tặng quà', en: 'Gift' },
    { id: 'sax', e: '🎷', vi: 'Saxophone', en: 'Sax' },
    { id: 'trumpet', e: '🎺', vi: 'Kèn vang', en: 'Fanfare' },
    { id: 'dragon', e: '🐉', vi: 'Rồng lửa', en: 'Dragon' },
    { id: 'catlove', e: '😻', vi: 'Mèo mê', en: 'Cat love' },
    { id: 'skull', e: '💀', vi: 'Xỉu ngang', en: 'Dead 💀' },
    { id: 'ufo', e: '🛸', vi: 'Đĩa bay', en: 'UFO' },
    { id: 'clover', e: '🍀', vi: 'May mắn', en: 'Good luck' },
    { id: 'firecracker', e: '🧨', vi: 'Pháo tết', en: 'Firecrackers' },
    { id: 'bomb', e: '💣', vi: 'Bùm!', en: 'Boom!' },
    { id: 'hundred', e: '💯', vi: 'Điểm 100', en: 'Perfect' },
    { id: 'muscle', e: '💪', vi: 'Cực khỏe', en: 'Powerful' },
    // ---- 20 hieu ung moi (dot 3) ----
    { id: 'volcano', e: '🌋', vi: 'Núi lửa', en: 'Volcano' },
    { id: 'wave', e: '🌊', vi: 'Sóng biển', en: 'Big wave' },
    { id: 'galaxy', e: '🌌', vi: 'Dải ngân hà', en: 'Galaxy' },
    { id: 'sparkler', e: '🎇', vi: 'Pháo bông', en: 'Sparklers' },
    { id: 'lantern', e: '🏮', vi: 'Đèn lồng', en: 'Lanterns' },
    { id: 'lixi', e: '🧧', vi: 'Lì xì', en: 'Red envelopes' },
    { id: 'xmas', e: '🎄', vi: 'Giáng sinh', en: 'Christmas' },
    { id: 'tiger', e: '🐯', vi: 'Hổ gầm', en: 'Tiger roar' },
    { id: 'panda', e: '🐼', vi: 'Gấu trúc', en: 'Pandas' },
    { id: 'frog', e: '🐸', vi: 'Ếch nhảy', en: 'Hopping frogs' },
    { id: 'eagle', e: '🦅', vi: 'Đại bàng', en: 'Eagle' },
    { id: 'burger', e: '🍔', vi: 'Mưa burger', en: 'Burger rain' },
    { id: 'coffee', e: '☕', vi: 'Cà phê', en: 'Coffee' },
    { id: 'sunflower', e: '🌻', vi: 'Hướng dương', en: 'Sunflowers' },
    { id: 'ring', e: '💍', vi: 'Nhẫn cưới', en: 'Wedding ring' },
    { id: 'medal', e: '🥇', vi: 'Huy chương', en: 'Gold medal' },
    { id: 'racecar', e: '🏎️', vi: 'Xe đua', en: 'Race car' },
    { id: 'plane', e: '✈️', vi: 'Máy bay', en: 'Airplane' },
    { id: 'pray', e: '🙏', vi: 'Cảm ơn', en: 'Thank you' },
    { id: 'mindblow', e: '🤯', vi: 'Nổ não', en: 'Mind blown' }
  ];
  function renderReactions() {
    var g = $('r-grid'); g.textContent = '';
    REACTIONS.forEach(function (r) {
      var b = el('button', 'rbtn'); b.type = 'button';
      b.appendChild(el('span', 're', r.e));
      b.appendChild(el('span', 'rl', lang === 'vi' ? r.vi : r.en));
      b.setAttribute('aria-label', lang === 'vi' ? r.vi : r.en);
      b.addEventListener('click', function () { sendReact(r.id, b); });
      g.appendChild(b);
    });
  }
  var reactErrAt = 0;
  function sendReact(id, btn) {
    btn.classList.remove('sent'); void btn.offsetWidth; btn.classList.add('sent');
    try { if (navigator.vibrate) navigator.vibrate(18); } catch (e) {}
    api('POST', '/api/react', { id: id }).catch(function (e) {
      var now = Date.now();
      if (now - reactErrAt > 3000) { reactErrAt = now; toast(e && e.status ? errText(e) : t('react_fail')); }
    });
  }

  // ------------------------------------------------------------ tin nhan chay chu tren man hinh TV
  var msgBusy = false;
  function sendMsg(text) {
    text = (text || '').replace(/\s+/g, ' ').trim();
    if (!text || msgBusy) return;
    msgBusy = true;
    api('POST', '/api/message', { text: text }).then(function (r) {
      if (r && r.dropped) { toast(t('msg_wait')); return; }
      $('r-msg').value = '';
      pill('✓ ' + t('msg_sent'));
      try { if (navigator.vibrate) navigator.vibrate(18); } catch (e) {}
    }).catch(function (e) {
      toast(e && e.status ? errText(e) : t('react_fail'));
    }).then(function () { setTimeout(function () { msgBusy = false; }, 600); });
  }
  var QUICK_COUNT = 35;
  function renderQuick() {
    var box = $('r-quick'); box.textContent = '';
    for (var qi = 1; qi <= QUICK_COUNT; qi++) (function (k) {
      var b = el('button', 'qchip', t(k)); b.type = 'button';
      b.addEventListener('click', function () { sendMsg(t(k)); });
      box.appendChild(b);
    })('q' + qi);
  }

  // ------------------------------------------------------------ search
  var reqId = 0, page = 0, hasMore = false, loading = false, curQuery = '', curRaw = '', results = [];
  var searchMode = pref('smode') || 'song';          // song | channel | lyrics
  var genre = pref('genre') || '';                   // tu khoa the loai ('' = tat ca)
  var recentOpen = pref('recentOpen') === '1';
  var fsVal = Number(pref('fs')) || 1.2;
  var hcOn = pref('hc') === '1';
  var themeVal = pref('theme') || 'auto';
  var uiVal = pref('ui') || 'new';
  var FS = [{ v: 1, l: 'fs_m' }, { v: 1.2, l: 'fs_l' }, { v: 1.4, l: 'fs_xl' }];
  function applyView() {
    var r = document.documentElement;
    r.style.setProperty('--fs', String(fsVal));
    if (hcOn) r.setAttribute('data-hc', '1'); else r.removeAttribute('data-hc');
    if (uiVal === 'classic') r.setAttribute('data-ui', 'classic'); else r.removeAttribute('data-ui');
    if (themeVal === 'light' || themeVal === 'dark') r.setAttribute('data-theme', themeVal); else r.removeAttribute('data-theme');
  }
  function renderFsChips() {
    var box = $('d-fs'); box.textContent = '';
    FS.forEach(function (f) {
      var b = el('button', fsVal === f.v ? 'on' : null, t(f.l));
      b.addEventListener('click', function () { fsVal = f.v; pref('fs', String(f.v)); applyView(); renderFsChips(); });
      box.appendChild(b);
    });
  }
  function renderUiChips() {
    var box = $('d-ui'); box.textContent = '';
    [['new', 'ui_new'], ['classic', 'ui_old']].forEach(function (x) {
      var b = el('button', uiVal === x[0] ? 'on' : null, t(x[1]));
      b.addEventListener('click', function () { uiVal = x[0]; pref('ui', x[0]); applyView(); renderUiChips(); });
      box.appendChild(b);
    });
  }
  function renderThemeChips() {
    var box = $('d-theme'); box.textContent = '';
    [['auto', 'th_auto'], ['light', 'th_light'], ['dark', 'th_dark']].forEach(function (x) {
      var b = el('button', themeVal === x[0] ? 'on' : null, t(x[1]));
      b.addEventListener('click', function () { themeVal = x[0]; pref('theme', x[0]); applyView(); renderThemeChips(); });
      box.appendChild(b);
    });
  }
  function setLbl(id, key) { var b = $(id), l = b.querySelector('.lbl'); if (l) l.textContent = t(key); }
  var GENRES = [
    { k: '', vi: 'Tất cả', en: 'All' }, { k: 'remix', vi: 'Remix', en: 'Remix' }, { k: 'bolero', vi: 'Bolero', en: 'Bolero' },
    { k: 'ballad', vi: 'Ballad', en: 'Ballad' }, { k: 'nhạc trẻ', vi: 'Nhạc trẻ', en: 'V-pop' }, { k: 'trữ tình', vi: 'Trữ tình', en: 'Lyrical' },
    { k: 'nhạc vàng', vi: 'Nhạc vàng', en: 'Golden oldies' }, { k: 'rumba', vi: 'Rumba', en: 'Rumba' }, { k: 'EDM', vi: 'EDM', en: 'EDM' },
    { k: 'nhạc sống', vi: 'Nhạc sống', en: 'Live band' }, { k: 'nhạc Hoa', vi: 'Nhạc Hoa', en: 'Chinese' }, { k: 'nhạc Hàn', vi: 'Nhạc Hàn', en: 'K-pop' },
    { k: 'tiếng Anh', vi: 'Nhạc Anh', en: 'English' }, { k: 'thiếu nhi', vi: 'Thiếu nhi', en: 'Kids' }
  ];
  var MODES = [{ k: 'song', l: 'm_song' }, { k: 'channel', l: 'm_channel' }, { k: 'lyrics', l: 'm_lyrics' }];
  function effQuery(q) {
    q = (q || '').trim();
    if (q && searchMode === 'lyrics') q = '"' + q + '" lời bài hát';
    return (q + (genre ? ' ' + genre : '')).trim();
  }
  function genreLabel(k) { for (var i = 0; i < GENRES.length; i++) if (GENRES[i].k === k) return GENRES[i][lang] || GENRES[i].vi; return k; }
  function renderFilterChips() {
    var mb = $('y-modes'); mb.textContent = '';
    MODES.forEach(function (m) {
      var b = el('button', searchMode === m.k ? 'on' : null, t(m.l));
      b.addEventListener('click', function () { searchMode = m.k; pref('smode', m.k); renderFilterChips(); filterChanged(); });
      mb.appendChild(b);
    });
    var gb = $('y-genres'); gb.textContent = '';
    GENRES.forEach(function (g) {
      var b = el('button', genre === g.k ? 'on' : null, g[lang] || g.vi);
      b.addEventListener('click', function () {
        genre = g.k; pref('genre', g.k); renderFilterChips(); updateFilterUi();
        closeAll(); clearTimeout(debounce); showTab('search');
        try { $('view-search').parentNode.scrollTop = 0; } catch (e) {}
        doSearch($('q').value, true); renderRecent();
      });
      gb.appendChild(b);
    });
  }
  function updateFilterUi() {
    var active = searchMode !== 'song' || !!genre;
    $('btn-yt').classList.toggle('on', active);
    var fl = $('filter-line');
    if (!active) { fl.hidden = true; return; }
    var parts = [];
    if (searchMode !== 'song') parts.push(t(searchMode === 'channel' ? 'm_channel' : 'm_lyrics'));
    if (genre) parts.push(genreLabel(genre));
    fl.textContent = '⚙ ' + t('filter_label') + ': ' + parts.join(' · ') + ' (' + t('filter_clear') + ')';
    fl.hidden = false;
  }
  function filterChanged() {
    updateFilterUi();
    clearTimeout(debounce);
    debounce = setTimeout(function () { showTab('search'); doSearch($('q').value, true); renderRecent(); }, 400);
  }
  function clearFilters() {
    searchMode = 'song'; genre = ''; pref('smode', 'song'); pref('genre', '');
    renderFilterChips(); filterChanged();
  }
  function searchChannel(name) {
    if (!name) return;
    searchMode = 'channel'; genre = ''; pref('smode', 'channel'); pref('genre', '');
    $('q').value = name; renderFilterChips(); onInput(true);
  }
  function openChannel(it) {
    api('GET', '/api/channel-url?videoId=' + encodeURIComponent(it.videoId)).then(function (r) {
      location.href = r.url || ('https://www.youtube.com/results?sp=EgIQAg%253D%253D&search_query=' + encodeURIComponent(it.channel || ''));
    }).catch(function () { toast(t('err_generic')); });
  }
  function channelEntries(it) {
    return [
      { label: t('open_channel'), run: function () { searchChannel(it.channel); } },
      { label: t('yt_channel'), run: function () { openChannel(it); } }
    ];
  }
  var debounce = null;

  function recents() { try { return JSON.parse(pref('recent') || '[]'); } catch (e) { return []; } }
  function pushRecent(q) {
    var r = recents().filter(function (x) { return x !== q; }); r.unshift(q);
    pref('recent', JSON.stringify(r.slice(0, 8)));
  }
  function renderRecent() {
    var box = $('recent'); box.textContent = '';
    var wrap = $('recent-wrap'), list = recents();
    if ($('q').value.trim() || !list.length) { wrap.hidden = true; return; }
    wrap.hidden = false;
    box.classList.toggle('open', recentOpen);
    $('recent-toggle').textContent = recentOpen ? '▴' : '▾';
    list.forEach(function (q) {
      var b = el('button', null, q);
      b.addEventListener('click', function () { $('q').value = q; onInput(true); });
      box.appendChild(b);
    });
  }

  var YT_RE = /(?:youtu\.be\/|youtube\.com\/(?:watch\?(?:[^#\s]*&)?v=|shorts\/|embed\/|live\/|v\/)|music\.youtube\.com\/watch\?(?:[^#\s]*&)?v=)([A-Za-z0-9_-]{11})/i;
  function ytLink(q) { var m = YT_RE.exec(q); return m ? m[1] : null; }

  function showLinkCard(q) {
    var my = ++reqId;
    var box = $('results'); box.textContent = ''; results = []; hasMore = false; loading = false;
    $('results-status').textContent = t('link_loading');
    api('POST', '/api/resolve-url', { url: q }).then(function (it) {
      if (my !== reqId) return;
      $('results-status').textContent = '';
      var card = el('li', 'linkcard');
      var head = el('div', 'lc-head'); head.innerHTML = IC.link; head.appendChild(el('span', null, t('link_found')));
      card.appendChild(head);
      var row = el('div', 'row-item'); row.appendChild(thumbBox(it));
      var col = el('div', 'info-col'); col.appendChild(el('div', 'title', it.title)); col.appendChild(el('div', 'sub', it.channel || ''));
      row.appendChild(col); card.appendChild(row);
      var btns = el('div', 'btns');
      var add = el('button', 'primary', t('add_end'));
      add.addEventListener('click', function () { addSong(it, 'end', false).then(function () { showTab('queue'); }); });
      var nxt = el('button', 'ghost', t('add_next'));
      nxt.addEventListener('click', function () { addSong(it, 'next', false).then(function () { showTab('queue'); }); });
      btns.appendChild(add); btns.appendChild(nxt); card.appendChild(btns);
      box.appendChild(card);
    }).catch(function (e) {
      if (my !== reqId) return;
      $('results-status').textContent = errText(e);
    });
  }

  function doSearch(q, reset) {
    q = q.trim();
    if (q && reset && ytLink(q)) { showLinkCard(q); return; }
    var eq = effQuery(q);
    if (!eq) { ++reqId; results = []; curQuery = ''; curRaw = ''; hasMore = false; loading = false; $('results').textContent = ''; $('results-status').textContent = ''; renderRecent(); return; }
    if (reset) { page = 0; results = []; hasMore = false; curQuery = eq; curRaw = q; $('results').textContent = ''; if (q) pushRecentLater(q); }
    var my = ++reqId;
    loading = true;
    $('results-status').textContent = '';
    var sk = el('li', 'skeleton'); sk.id = 'sk'; $('results').appendChild(sk);
    api('GET', '/api/search?q=' + encodeURIComponent(eq) + '&page=' + page + '&karaoke=' + (karaoke ? 1 : 0) + ((searchMode === 'channel' && q) ? '&channel=' + encodeURIComponent(q) : ''))
      .then(function (r) {
        if (my !== reqId) return;
        removeSk();
        loading = false;
        hasMore = r.hasMore;
        r.results.forEach(function (it) { results.push(it); appendResult(it); });
        if (!results.length) $('results-status').textContent = t('no_results');
        page = r.page + 1;
      }).catch(function (e) {
        if (my !== reqId) return;
        removeSk(); loading = false; hasMore = false;
        $('results-status').textContent = errText(e);
      });
  }
  var recentTimer = null;
  function pushRecentLater(q) { clearTimeout(recentTimer); recentTimer = setTimeout(function () { if (q === curRaw) pushRecent(q); }, 1500); }
  function removeSk() { var s = $('sk'); if (s) s.remove(); }

  function songRow(it, inFavs) {
    var li = el('li', 'row-item'); li.dataset.vid = it.videoId;
    li.appendChild(thumbBox(it));
    var col = el('div', 'info-col');
    col.appendChild(el('div', 'title', it.title));
    col.appendChild(el('div', 'sub', it.channel));
    var badge = el('div', 'badge', '✓ ' + t('in_queue')); badge.hidden = true; col.appendChild(badge);
    li.appendChild(col);
    var plus = el('button', 'act plus', '+'); plus.setAttribute('aria-label', t('add'));
    var more = el('button', 'act more', '⋮'); more.setAttribute('aria-label', 'Menu');
    li.appendChild(actsBox([plus, more, favBtn(it)]));
    li.addEventListener('click', function (ev) {
      var tg = ev.target;
      // bam vao nut ba cham / trai tim / khoang trong cua cum nut thi khong tu dong them bai
      if (tg && tg.closest && tg.closest('.acts') && !tg.closest('.plus')) return;
      addSong(it, 'end', false);
    });
    more.addEventListener('click', function (ev) { ev.stopPropagation(); openResultMenu(it); });
    li.addEventListener('contextmenu', function (ev) { ev.preventDefault(); openResultMenu(it); });
    markBadge(li, !!inQueueSet()[it.videoId] || it.alreadyQueued);
    return li;
  }
  function appendResult(it) { $('results').appendChild(songRow(it, false)); }
  function markBadge(li, queued) {
    var badge = li.querySelector('.badge'); var plus = li.querySelector('.act.plus');
    badge.hidden = !queued; plus.textContent = queued ? '✓' : '+';
  }
  function refreshResultBadges() {
    var set = inQueueSet();
    var items = document.querySelectorAll('#results li.row-item, #fav-list li.row-item');
    for (var i = 0; i < items.length; i++) markBadge(items[i], !!set[items[i].dataset.vid]);
  }

  function onInput(immediate) {
    var v = $('q').value;
    $('q-clear').hidden = !v;
    updateFilterUi();
    showTab('search');
    clearTimeout(debounce);
    debounce = setTimeout(function () { doSearch(v, true); }, immediate ? 0 : 300);
    renderRecent();
  }

  // infinite scroll
  var io = null;
  function setupScroll() {
    if (!('IntersectionObserver' in window)) return;
    io = new IntersectionObserver(function (entries) {
      if (entries[0].isIntersecting && hasMore && !loading && curQuery) doSearch(curRaw, false);
    }, { rootMargin: '300px' });
    io.observe($('sentinel'));
  }

  // ------------------------------------------------------------ tabs / sheets
  function showTab(name) {
    ['search', 'queue', 'favs'].forEach(function (n) {
      $('view-' + n).hidden = (n !== name);
      $('tab-' + n).classList.toggle('active', n === name);
    });
    if (name !== 'search') { $('view-' + name).parentNode.scrollTop = 0; }
    if (name === 'favs') renderFavs();
  }
  function openSheet(id) { $('scrim').hidden = false; $(id).hidden = false; }
  function closeAll() {
    ['drawer', 'sheet-yt', 'sheet-add', 'sheet-menu', 'sheet-react', 'donate-big'].forEach(function (i) { $(i).hidden = true; });
    $('scrim').hidden = true;
    try { $('r-msg').blur(); } catch (e) {}
  }

  function openMenu(title, entries) {
    $('m-title').textContent = title;
    var box = $('m-items'); box.textContent = '';
    entries.forEach(function (e) {
      var b = el('button', e.bad ? 'bad' : null, e.label);
      b.addEventListener('click', function () { closeAll(); e.run(); });
      box.appendChild(b);
    });
    openSheet('sheet-menu');
  }
  function openResultMenu(it) {
    openMenu(it.title, [
      { label: t('add'), run: function () { addSong(it, 'end', false); } },
      { label: t('play_next'), run: function () { addSong(it, 'next', false); } },
      { label: t('play_now'), run: function () {
          confirmIfNeeded(function () {
            api('POST', '/api/queue', { videoId: it.videoId, title: it.title, channel: it.channel,
              durationSec: it.durationSec, position: 'next', addedBy: device, force: true },
              { 'Idempotency-Key': uuid() })
              .then(function (r) {
                var added = r.item; return command('POST', '/api/queue/' + added.itemId + '/play', {});
              }).catch(function (e) { toast(errText(e)); });
          });
        } },
      favEntry(it)
    ].concat(channelEntries(it)));
  }
  function openNowMenu(it) {
    openMenu(it.title, channelEntries(it).concat([favEntry(it)]));
  }
  function openQueueMenu(it, idx) {
    var list = [
      { label: t('play_now'), run: function () { confirmIfNeeded(function () { command('POST', '/api/queue/' + it.itemId + '/play', {}); }); } },
      { label: t('prioritize'), run: function () { command('POST', '/api/queue/' + it.itemId + '/prioritize', {}); } }
    ];
    if (idx > 0) list.push({ label: '↑ ' + t('move_up'), run: function () { command('POST', '/api/queue/reorder', { itemId: it.itemId, newIndex: idx - 1 }); } });
    if (S && idx < S.queue.length - 1) list.push({ label: '↓ ' + t('move_down'), run: function () { command('POST', '/api/queue/reorder', { itemId: it.itemId, newIndex: idx + 1 }); } });
    list.push(favEntry(it));
    channelEntries(it).forEach(function (e) { list.push(e); });
    list.push({ label: t('remove'), bad: true, run: function () { command('DELETE', '/api/queue/' + it.itemId); } });
    openMenu(it.title, list);
  }

  // ------------------------------------------------------------ transport
  function volume(delta) {
    api('POST', '/api/player/volume', { delta: delta }).then(function (r) {
      if (r.state) applyState(r.state);
      pill(t('vol') + r.level);
    }).catch(function () {});
  }
  function holdRepeat(btn, delta) {
    var timer = null, fired = false;
    function stop() { clearInterval(timer); timer = null; }
    btn.addEventListener('pointerdown', function (e) {
      e.preventDefault(); fired = true; volume(delta);
      stop(); timer = setInterval(function () { volume(delta); }, 150);
    });
    ['pointerup', 'pointerleave', 'pointercancel'].forEach(function (ev) { btn.addEventListener(ev, stop); });
  }

  // ------------------------------------------------------------ voice search (mic o cuoi o tim kiem)
  var recog = null, listening = false;
  function setMicOn(on) {
    listening = on;
    var m = $('q-mic');
    if (m) m.classList.toggle('on', on);
  }
  function micClick() {
    var SR = window.SpeechRecognition || window.webkitSpeechRecognition;
    if (!SR) { showTab('search'); $('q').focus(); toast(t('mic_unsupported')); return; }
    if (listening && recog) { try { recog.stop(); } catch (e) {} return; }
    showTab('search');
    var r;
    try { r = new SR(); } catch (e) { toast(t('mic_unsupported')); return; }
    recog = r;
    r.lang = lang === 'vi' ? 'vi-VN' : 'en-US';
    r.continuous = false;
    r.interimResults = true;
    r.maxAlternatives = 1;
    var finalText = '', searched = false, errShown = false;
    r.onstart = function () { setMicOn(true); toast(t('listening')); };
    r.onresult = function (e) {
      var txt = '';
      for (var k = 0; k < e.results.length; k++) txt += e.results[k][0].transcript;
      finalText = txt;
      $('q').value = txt;
      $('q-clear').hidden = !txt;
      if (e.results[e.results.length - 1].isFinal) { searched = true; onInput(true); }
    };
    r.onerror = function (e) {
      var code = e && e.error;
      if (code !== 'aborted') errShown = true;
      if (code === 'not-allowed' || code === 'service-not-allowed') toast(t('mic_denied'));
      else if (code === 'no-speech') toast(t('no_speech'));
      else if (code !== 'aborted') toast(t('mic_unsupported'));
    };
    r.onend = function () {
      setMicOn(false);
      if (!errShown) { var tb = $('toast'); if (tb) tb.hidden = true; }
      if (finalText && !searched && $('q').value === finalText) onInput(true);
      recog = null;
    };
    try {
      r.start();   // trinh duyet se hoi cho phep truy cap micro
    } catch (e) {
      setMicOn(false);
      toast(t('mic_unsupported'));
    }
  }

  // ------------------------------------------------------------ texts
  function applyLang() {
    document.documentElement.lang = lang;
    $('q').placeholder = t('search_ph');
    $('tab-search').textContent = t('tab_search');
    $('tab-queue').textContent = t('tab_queue') + ' (' + (S ? S.queue.length : 0) + ')';
    $('tab-favs').textContent = t('tab_favs');
    $('r-title').textContent = t('react_title');
    $('r-hint').textContent = t('react_hint');
    $('r-close').textContent = t('close');
    $('r-msg').placeholder = t('msg_ph');
    $('r-send').textContent = t('msg_send');
    renderQuick();
    renderReactions();
    syncFavs();
    renderFavs();
    $('d-title').textContent = t('menu');
    $('d-lang-l').textContent = t('language');
    $('d-dev-l').textContent = t('device');
    $('d-karaoke-l').textContent = t('karaoke_mode');
    $('d-vol-l').textContent = t('volume_slider');
    $('d-res-l').textContent = t('res_play');
    $('d-reset').textContent = t('reset_pairing');
    $('d-close').textContent = t('close');
    $('donate-text').textContent = t('donate_text');
    $('y-title').textContent = t('source') + ': ' + t('yt_src');
    $('y-karaoke-l').textContent = t('karaoke_mode');
    $('y-mode-l').textContent = t('search_by');
    setLbl('b-next', 'l_next'); setLbl('b-replay', 'l_replay'); setLbl('b-mic', 'l_mic'); setLbl('b-vdown', 'l_vdown');
    setLbl('b-vup', 'l_vup'); setLbl('b-react', 'l_react'); setLbl('b-add', 'l_link');
    setPlayLbl();
    $('d-theme-l').textContent = t('theme'); renderThemeChips();
    $('d-ui-l').textContent = t('ui_mode'); renderUiChips();
    $('d-fs-l').textContent = t('text_size'); $('d-hc-l').textContent = t('hc_mode'); renderFsChips();
    $('y-genre-l').textContent = t('genre');
    renderFilterChips(); updateFilterUi();
    $('y-close').textContent = t('close');
    $('a-title').textContent = t('paste_title');
    $('a-url').placeholder = t('paste_ph');
    $('a-name').placeholder = t('title_opt');
    $('a-ok').textContent = t('add');
    $('a-cancel').textContent = t('cancel');
    $('m-close').textContent = t('close');
    $('queue-empty').textContent = (S && S.queue.length) ? '' : t('nothing');
    renderRecent();
    lastQueueSig = '';
    if (S) renderQueue();
  }

  function fillInfo() {
    var lines = [];
    lines.push(t('conn') + ': ' + location.host);
    if (S) {
      lines.push(t('backend') + ': ' + S.backend + ' (' + S.settings.backend + ')');
      lines.push(t('res') + ': ' + S.settings.maxHeight + 'p');
      lines.push('Ticker: ' + S.settings.tickerMode);
    }
    lines.push(t('about') + ': Savify Karaoke');
    $('d-info').textContent = lines.join('\n');
    $('d-info').style.whiteSpace = 'pre-line';
  }

  // ------------------------------------------------------------ wire up
  function init() {
    setIcon('b-play', 'play'); setIcon('b-next', 'next'); setIcon('b-replay', 'replay'); setIcon('b-mic', 'mic'); setIcon('q-mic', 'mic');
    setIcon('b-vdown', 'vdown'); setIcon('b-vup', 'vup'); setIcon('b-react', 'react'); setIcon('b-add', 'link');
    $('d-lang').value = lang;
    $('d-dev').value = device;
    $('d-karaoke').checked = karaoke; $('y-karaoke').checked = karaoke;
    applyLang();

    $('search-form').addEventListener('submit', function (e) { e.preventDefault(); $('q').blur(); onInput(true); });
    $('q').addEventListener('input', function () { onInput(false); });
    $('q').addEventListener('focus', function () { showTab('search'); });
    $('q-clear').addEventListener('click', function () { $('q').value = ''; onInput(true); $('q').focus(); });

    $('tab-search').addEventListener('click', function () { showTab('search'); });
    $('tab-queue').addEventListener('click', function () { showTab('queue'); });
    $('tab-favs').addEventListener('click', function () { showTab('favs'); });

    $('btn-menu').addEventListener('click', function () { fillInfo(); openSheet('drawer'); });
    $('btn-yt').addEventListener('click', function () { openSheet('sheet-yt'); });
    $('scrim').addEventListener('click', closeAll);
    $('d-close').addEventListener('click', closeAll);
    $('donate-qr').addEventListener('click', function () { $('donate-big').hidden = false; });
    $('donate-big').addEventListener('click', function () { $('donate-big').hidden = true; });
    $('y-close').addEventListener('click', closeAll);
    $('m-close').addEventListener('click', closeAll);
    $('a-cancel').addEventListener('click', closeAll);

    $('d-lang').addEventListener('change', function () { lang = $('d-lang').value; pref('lang', lang); applyLang(); });
    $('d-dev').addEventListener('change', function () {
      var v = $('d-dev').value.trim().slice(0, 30); if (v) { device = v; pref('device', v); }
    });
    function setKaraoke(v) {
      karaoke = v; pref('karaoke', v ? '1' : '0');
      $('d-karaoke').checked = v; $('y-karaoke').checked = v;
      if (curQuery) doSearch($('q').value, true);
    }
    $('d-karaoke').addEventListener('change', function () { setKaraoke($('d-karaoke').checked); });
    $('y-karaoke').addEventListener('change', function () { setKaraoke($('y-karaoke').checked); });
    $('d-vol').addEventListener('change', function () {
      api('POST', '/api/player/volume', { level: Number($('d-vol').value) }).then(function (r) { if (r.state) applyState(r.state); });
    });
    $('d-hc').checked = hcOn;
    $('d-hc').addEventListener('change', function () { hcOn = $('d-hc').checked; pref('hc', hcOn ? '1' : '0'); applyView(); });
    applyView();
    // an thanh dieu khien khi dang go chu de ban phim khong che danh sach
    $('q').addEventListener('focus', function () { document.body.classList.add('typing'); });
    $('q').addEventListener('blur', function () { document.body.classList.remove('typing'); });
    // rung nhe khi bam nut (dien thoai Android) de biet la da bam
    document.addEventListener('click', function (e) {
      if (e.target && e.target.closest && e.target.closest('button') && navigator.vibrate) { try { navigator.vibrate(18); } catch (x) {} }
    }, true);
    $('recent-toggle').addEventListener('click', function () { recentOpen = !recentOpen; pref('recentOpen', recentOpen ? '1' : '0'); renderRecent(); });
    $('filter-line').addEventListener('click', clearFilters);
    var rg = $('mini-range');
    rg.addEventListener('pointerdown', function () { seeking = true; clearTimeout(seekTimer); });
    rg.addEventListener('input', function () {
      seeking = true;
      if (S && S.durationMs > 0) $('mini-pos').textContent = fmt(Number(rg.value) * S.durationMs / 1000000);
    });
    rg.addEventListener('change', function () {
      if (!S || !(S.durationMs > 0)) { seeking = false; return; }
      var pos = Math.round(Number(rg.value) * S.durationMs / 1000);
      command('POST', '/api/player/seek', { positionMs: pos });
      clearTimeout(seekTimer); seekTimer = setTimeout(function () { seeking = false; }, 1200);
    });
    $('mini-back').addEventListener('click', function () { command('POST', '/api/player/seek', { deltaMs: -10000 }); });
    $('mini-fwd').addEventListener('click', function () { command('POST', '/api/player/seek', { deltaMs: 10000 }); });
    $('d-res').addEventListener('change', function () {
      api('POST', '/api/settings', { maxHeight: Number($('d-res').value) }).then(function (r) { if (r.state) applyState(r.state); }).catch(function () {});
    });
    $('d-reset').addEventListener('click', function () {
      if (!confirm(t('reset_confirm'))) return;
      api('POST', '/api/pairing/reset', {}).then(function (r) { token = r.token; closeAll(); toast('OK'); })
        .catch(function (e) { toast(errText(e)); });
    });

    $('b-play').addEventListener('click', function () {
      var playing = S && S.state === 'PLAYING';
      command('POST', playing ? '/api/player/pause' : '/api/player/resume', {});
    });
    $('b-next').addEventListener('click', function () { confirmIfNeeded(function () { command('POST', '/api/player/skip', {}); }); });
    $('b-replay').addEventListener('click', function () { command('POST', '/api/player/restart', {}); });
    $('b-mic').addEventListener('click', micClick);
    $('q-mic').addEventListener('click', micClick);
    holdRepeat($('b-vdown'), -5);
    holdRepeat($('b-vup'), 5);
    $('b-react').addEventListener('click', function () { openSheet('sheet-react'); });
    $('r-close').addEventListener('click', closeAll);
    $('r-msgform').addEventListener('submit', function (e) { e.preventDefault(); sendMsg($('r-msg').value); });
    $('b-add').addEventListener('click', function () { $('a-url').value = ''; $('a-name').value = ''; openSheet('sheet-add'); setTimeout(function () { $('a-url').focus(); }, 50); });

    $('a-ok').addEventListener('click', function () {
      var url = $('a-url').value.trim(); var name = $('a-name').value.trim();
      if (!url) { toast(t('invalid_url')); return; }
      var body = { url: url, position: 'end', addedBy: device };
      if (name) body.title = name;
      api('POST', '/api/queue', body, { 'Idempotency-Key': uuid() }).then(function (r) {
        closeAll();
        if (r.state) applyState(r.state);
        toast(t('added') + (r.item ? r.item.title : ''), null, null, 'ok');
        showTab('queue');
      }).catch(function (e) {
        if (e.code === 'DUPLICATE_PENDING') {
          body.force = true;
          toast(t('dup_confirm'), t('yes'), function () {
            api('POST', '/api/queue', body, { 'Idempotency-Key': uuid() }).then(function (r) { closeAll(); if (r.state) applyState(r.state); showTab('queue'); });
          });
        } else toast(errText(e));
      });
    });

    setupScroll();
    poll();
  }

  init();
})();
