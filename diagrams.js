// Diagramy w stylu tablicy Miro: karteczki, ramki, strzałki, dymki Agi i suwak spójność ↔ skalowalność.
(() => {
  const S = 88;          // bok karteczki
  const P = 110;         // odstęp wierszy
  const C = {
    blue: '#a6e5fa', yellow: '#fff176', orange: '#ffb57a', red: '#ff9d9d', teal: '#7fe3d9',
    navy: '#050038', box: '#e6e6f0', boxLine: '#d2d2e0', gray: '#8a89a6', pink: '#ffe3e3', aga: '#e5487a',
  };
  const esc = (t) => String(t).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
  const DIRS = { l: [-1, 0], r: [1, 0], t: [0, -1], b: [0, 1] };

  class Canvas {
    constructor() { this.items = []; this.pos = {}; this.bb = [Infinity, Infinity, -Infinity, -Infinity]; }
    ext(x, y, w, h) {
      const b = this.bb;
      b[0] = Math.min(b[0], x); b[1] = Math.min(b[1], y); b[2] = Math.max(b[2], x + w); b[3] = Math.max(b[3], y + h);
    }
    add(z, svg) { this.items.push({ z, svg }); }
    place(id, x, y, w, h) { this.pos[id] = { x, y, w, h }; this.ext(x, y, w, h); }

    box(id, x, y, w, h, title) {
      this.place(id, x, y, w, h);
      this.add(0, `<rect x="${x}" y="${y}" width="${w}" height="${h}" rx="18" fill="${C.box}" stroke="${C.boxLine}" stroke-width="1.5"/>
        <text x="${x + 20}" y="${y + 33}" class="dg-title">${esc(title)}</text>`);
    }
    sticky(id, x, y, color, text, o = {}) {
      const w = o.w || S, h = o.h || S;
      this.place(id, x, y, w, h);
      this.add(2, `<rect x="${x + 1}" y="${y + 4}" width="${w}" height="${h}" rx="3" fill="rgba(5,0,56,.10)"/>
        <rect x="${x}" y="${y}" width="${w}" height="${h}" rx="3" fill="${C[color]}"/>
        <foreignObject x="${x}" y="${y}" width="${w}" height="${h}"><div xmlns="http://www.w3.org/1999/xhtml" class="dg-text" style="font-size:${o.fs || 12.5}px">${esc(text)}</div></foreignObject>`);
    }
    ghost(id, x, y, text) {
      this.place(id, x, y, S, S);
      this.add(2, `<rect x="${x}" y="${y}" width="${S}" height="${S}" rx="3" fill="rgba(255,255,255,.55)" stroke="${C.gray}" stroke-width="1.5" stroke-dasharray="5 4"/>
        <foreignObject x="${x}" y="${y}" width="${S}" height="${S}"><div xmlns="http://www.w3.org/1999/xhtml" class="dg-text dg-ghost">${esc(text)}</div></foreignObject>`);
    }
    label(x, y, text, cls = 'dg-note') { this.add(3, `<text x="${x}" y="${y}" class="${cls}">${esc(text)}</text>`); }

    pt(ref) {
      if (Array.isArray(ref)) return { p: ref.slice(0, 2), d: DIRS[ref[2]] || [0, 0] };
      const [id, side, f] = ref.split(':');
      const r = this.pos[id];
      const t = f === undefined ? 0.5 : parseFloat(f);
      const p = { l: [r.x, r.y + r.h * t], r: [r.x + r.w, r.y + r.h * t], t: [r.x + r.w * t, r.y], b: [r.x + r.w * t, r.y + r.h] }[side];
      return { p, d: DIRS[side] };
    }
    arrow(a, b, o = {}) {
      const A = this.pt(a), B = this.pt(b);
      const dist = Math.hypot(B.p[0] - A.p[0], B.p[1] - A.p[1]);
      const k1 = o.k1 ?? o.k ?? Math.max(30, dist * 0.42);
      const k2 = o.k2 ?? o.k ?? Math.max(30, dist * 0.42);
      const c1 = [A.p[0] + A.d[0] * k1, A.p[1] + A.d[1] * k1];
      const c2 = [B.p[0] + B.d[0] * k2, B.p[1] + B.d[1] * k2];
      const kind = o.kind || 'solid';
      this.add(o.z ?? 1, `<path d="M${A.p} C${c1} ${c2} ${B.p}" class="dg-arrow dg-${kind}" marker-end="url(#dg-ah-${kind})"/>`);
    }
    bubble(id, x, y, w, h, text, tail) {
      // dymek z ogonkiem po lewej stronie, na wysokości tail (0..1)
      this.place(id, x, y, w, h);
      const ty = y + h * (tail ?? 0.5), r = 16;
      const d = `M${x + r},${y} H${x + w - r} Q${x + w},${y} ${x + w},${y + r} V${y + h - r} Q${x + w},${y + h} ${x + w - r},${y + h}
        H${x + r} Q${x},${y + h} ${x},${y + h - r} V${ty + 12} L${x - 22},${ty} L${x},${ty - 12} V${y + r} Q${x},${y} ${x + r},${y} Z`;
      this.add(3, `<path d="${d}" fill="${C.pink}" stroke="${C.red}" stroke-width="2"/>
        <circle cx="${x + w - 10}" cy="${y - 4}" r="22" fill="${C.aga}"/>
        <text x="${x + w - 10}" y="${y + 1}" text-anchor="middle" class="dg-aga">Aga</text>
        <foreignObject x="${x}" y="${y}" width="${w}" height="${h}"><div xmlns="http://www.w3.org/1999/xhtml" class="dg-bubble">„${esc(text)}”</div></foreignObject>`);
      this.ext(x - 24, y - 26, w + 40, h + 26);
    }
    slider(x, y, w, pos, caption, badge) {
      // pos: 0 = pełna spójność, 1 = pełna skalowalność
      const ty = y + 40, kx = x + w * pos;
      this.add(3, `<rect x="${x - 18}" y="${y - 6}" width="${w + 36}" height="${badge ? 124 : 104}" rx="14" fill="#fff" stroke="${C.boxLine}"/>
        <text x="${x}" y="${y + 20}" class="dg-sl">spójność</text>
        <text x="${x + w}" y="${y + 20}" text-anchor="end" class="dg-sl">skalowalność</text>
        <rect x="${x}" y="${ty - 4}" width="${w}" height="8" rx="4" fill="url(#dg-grad)"/>
        ${[0, .25, .5, .75, 1].map((t) => `<line x1="${x + w * t}" x2="${x + w * t}" y1="${ty + 9}" y2="${ty + 15}" stroke="${C.gray}" stroke-width="1.5"/>`).join('')}
        <circle cx="${kx}" cy="${ty}" r="15" fill="${C.teal}" stroke="${C.navy}" stroke-width="2.5"/>
        <text x="${x + w / 2}" y="${ty + 42}" text-anchor="middle" class="dg-cap">${esc(caption)}</text>
        ${badge ? `<text x="${x + w / 2}" y="${ty + 66}" text-anchor="middle" class="dg-cap dg-badge">${esc(badge)}</text>` : ''}`);
      this.ext(x - 18, y - 6, w + 36, badge ? 124 : 104);
    }
    code(id, x, y, w, h, html) {
      this.place(id, x, y, w, h);
      this.add(3, `<rect x="${x}" y="${y + 5}" width="${w}" height="${h}" rx="14" fill="rgba(5,0,56,.18)"/>
        <rect x="${x}" y="${y}" width="${w}" height="${h}" rx="14" fill="${C.navy}"/>
        <foreignObject x="${x}" y="${y}" width="${w}" height="${h}"><pre xmlns="http://www.w3.org/1999/xhtml" class="dg-code">${html}</pre></foreignObject>`);
    }
    card(id, x, y, w, h, title, text) {
      this.place(id, x, y, w, h);
      this.add(3, `<rect x="${x + 1}" y="${y + 5}" width="${w}" height="${h}" rx="4" fill="rgba(5,0,56,.12)"/>
        <rect x="${x}" y="${y}" width="${w}" height="${h}" rx="4" fill="${C.red}"/>
        <foreignObject x="${x}" y="${y}" width="${w}" height="${h}"><div xmlns="http://www.w3.org/1999/xhtml" class="dg-card"><b>${esc(title)}</b><span>${text}</span></div></foreignObject>`);
    }

    merge(o, dx, dy) {
      this.add(0, `<g transform="translate(${dx},${dy})">${o.items.sort((a, b) => a.z - b.z).map((i) => i.svg).join('\n')}</g>`);
      Object.entries(o.pos).forEach(([k, r]) => { this.pos[k] = { ...r, x: r.x + dx, y: r.y + dy }; });
      const b = o.bb; this.ext(b[0] + dx, b[1] + dy, b[2] - b[0], b[3] - b[1]);
    }
    big(x, y, lines) {
      this.add(3, `<text x="${x}" y="${y}" class="dg-big">${lines.map((l, i) => `<tspan x="${x}" dy="${i ? 36 : 0}">${esc(l)}</tspan>`).join('')}</text>`);
      this.ext(x, y - 28, 150, 36 * lines.length);
    }

    svg(label) {
      const pad = 26, [x0, y0, x1, y1] = this.bb;
      const vb = [x0 - pad, y0 - pad, x1 - x0 + 2 * pad, y1 - y0 + 2 * pad];
      const body = this.items.sort((a, b) => a.z - b.z).map((i) => i.svg).join('\n');
      return `<svg xmlns="http://www.w3.org/2000/svg" viewBox="${vb.join(' ')}" class="dg" role="img" aria-label="${esc(label)}" data-w="${vb[2]}">
<defs>
  <style>
    .dg-title { font: 700 19px Inter, system-ui, sans-serif; fill: ${C.navy}; }
    .dg-big { font: 500 30px Inter, system-ui, sans-serif; fill: ${C.navy}; letter-spacing: -.01em; }
    .dg-note { font: italic 500 13px Inter, system-ui, sans-serif; fill: #55547a; }
    .dg-text { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; text-align: center; box-sizing: border-box; padding: 5px 4px;
               font-family: Inter, system-ui, sans-serif; font-weight: 500; line-height: 1.17; color: ${C.navy}; overflow-wrap: break-word; hyphens: auto; }
    .dg-ghost { color: #6b6a8a; font-size: 11.5px; font-style: italic; }
    .dg-bubble { width: 100%; height: 100%; display: flex; align-items: center; box-sizing: border-box; padding: 14px 18px; font: 600 16px/1.35 Inter, system-ui, sans-serif; color: ${C.navy}; }
    .dg-aga { font: 800 12px Inter, system-ui, sans-serif; fill: #fff; }
    .dg-sl { font: 700 14px Inter, system-ui, sans-serif; fill: ${C.navy}; }
    .dg-cap { font: 600 14px Inter, system-ui, sans-serif; fill: #3d3c66; }
    .dg-badge { fill: #00897b; }
    .dg-code { margin: 0; padding: 18px 22px; box-sizing: border-box; height: 100%; font: 400 15.5px/1.6 "JetBrains Mono", ui-monospace, monospace; font-variant-ligatures: none; color: #e8e8ff; white-space: pre; }
    .dg-code .kw { color: #ffd02f; } .dg-code .fn { color: #7fe3d9; } .dg-code .hl { color: #ff9d9d; font-weight: 600; } .dg-code .cm { color: #8f8dbb; }
    .dg-card { width: 100%; height: 100%; box-sizing: border-box; padding: 14px; display: flex; flex-direction: column; justify-content: center; gap: 6px; text-align: center;
               font: 500 14px/1.3 Inter, system-ui, sans-serif; color: ${C.navy}; }
    .dg-card b { font-size: 17px; }
    .dg-card code { font: 600 13px "JetBrains Mono", monospace; background: rgba(255,255,255,.6); padding: 1px 4px; border-radius: 4px; }
    .dg-arrow { fill: none; }
    .dg-solid { stroke: ${C.navy}; stroke-width: 2.2; }
    .dg-dashed { stroke: #4b4a6b; stroke-width: 1.8; stroke-dasharray: 7 6; }
    .dg-red { stroke: ${C.aga}; stroke-width: 2.2; stroke-dasharray: 7 6; }
    .dg-code-arrow { stroke: ${C.navy}; stroke-width: 2.2; }
  </style>
  <marker id="dg-ah-solid" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse"><path d="M0,0 L10,5 L0,10 z" fill="${C.navy}"/></marker>
  <marker id="dg-ah-code-arrow" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse"><path d="M0,0 L10,5 L0,10 z" fill="${C.navy}"/></marker>
  <marker id="dg-ah-dashed" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse"><path d="M0,0 L10,5 L0,10 z" fill="#4b4a6b"/></marker>
  <marker id="dg-ah-red" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse"><path d="M0,0 L10,5 L0,10 z" fill="${C.aga}"/></marker>
  <linearGradient id="dg-grad" x1="0" x2="1"><stop offset="0" stop-color="#4262ff"/><stop offset="1" stop-color="#00b5a6"/></linearGradient>
</defs>
${body}
</svg>`;
    }
  }

  // ---------- Portfel: pełny (krok 2 i 3.1–3.2, wariant A) ----------
  const BOX_X = 118, BOX_W = 434, EV_X = 590;
  const RULE_X = [138, 240, 342, 444];

  function portfelFull(c, oy, { policy }) {
    const rows = [
      ['zreal', 'Zrealizuj punkty', 'Punkty zrealizowane'],
      ['nalicz', 'Nalicz punkty za zakup', 'Portfel doładowany'],
      ['dodaj', 'Dodaj prawo do portfela', 'Prawo do portfela dodane'],
      ['zabierz', 'Zabierz prawo do portfela', 'Prawo do portfela zabrane'],
      ['dezakt', 'Dezaktywuj portfel', 'Portfel zdezaktywowany'],
      ['akt', 'Aktywuj portfel', 'Portfel aktywowany'],
      ['okno', 'Otwórz nowe okno', 'Nowe okno otwarte'],
    ];
    if (policy) rows.push(['polityka', 'Zmień politykę naliczeń (tier)', 'Zmieniono politykę naliczeń']);
    const y = (i) => oy + 52 + i * P;
    c.box('portfel', BOX_X, oy, BOX_W, 52 + (rows.length - 1) * P + S + 28, 'Portfel');
    rows.forEach(([k, cmd, ev], i) => {
      c.sticky(k, 0, y(i), 'blue', cmd);
      c.sticky('e_' + k, EV_X, y(i), 'orange', ev);
    });
    c.sticky('e_odrz', EV_X + S + 16, y(0), 'orange', 'Realizacja odrzucona');
    c.sticky('r_akt0', RULE_X[0], y(0), 'yellow', 'Portfel aktywny');
    c.sticky('r_osoba', RULE_X[1], y(0), 'yellow', 'Osoba z dostępem do portfela');
    c.sticky('r_saldo', RULE_X[2], y(0), 'yellow', 'Punkty nie przekraczają salda');
    c.sticky('r_okno', RULE_X[3], y(0), 'yellow', 'Bieżące okno nie jest wyczerpane');
    c.sticky('r_akt1', RULE_X[0], y(1), 'yellow', 'Portfel aktywny');

    c.arrow('zreal:r', 'r_akt0:l');
    c.arrow('nalicz:r', 'r_akt1:l');
    // które zdarzenie zmienia dane, na których stoi reguła
    c.arrow('e_zreal:t', 'r_saldo:t', { kind: 'dashed', k: 70 });
    c.arrow('e_nalicz:l', 'r_saldo:b:0.7', { kind: 'dashed', k1: 90, k2: 50 });
    c.arrow('e_dodaj:l', 'r_osoba:b:0.35', { kind: 'dashed', k1: 120, k2: 90 });
    c.arrow('e_zabierz:l', 'r_osoba:b:0.65', { kind: 'dashed', k1: 150, k2: 140 });
    c.arrow('e_dezakt:l', 'r_akt1:b:0.4', { kind: 'dashed', k1: 160, k2: 160 });
    c.arrow('e_akt:l', 'r_akt1:b:0.6', { kind: 'dashed', k1: 200, k2: 220 });
    c.arrow('e_okno:l', 'r_okno:b', { kind: 'dashed', k1: 60, k2: 300 });
    if (policy) {
      c.arrow('e_polityka:l', 'r_saldo:b:0.35', { kind: 'dashed', k1: 110, k2: 420 });
      c.label(BOX_X + 22, y(rows.length - 1) + S + 10, 'zmiana polityki naliczeń wpływa na saldo');
    }
    return { bottom: oy + 52 + (rows.length - 1) * P + S + 28 };
  }

  // ---------- Portfel: bez dostępu (3.3, 3.4, wariant B) ----------
  const COMPACT_ROWS = [52, 162, 300, 410, 520];

  function portfelCompact(c, oy) {
    const rows = [
      ['zreal', 'Zrealizuj punkty', 'Punkty zrealizowane'],
      ['dolad', 'Doładuj', 'Portfel doładowany'],
      ['dezakt', 'Dezaktywuj portfel', 'Portfel zdezaktywowany'],
      ['akt', 'Aktywuj portfel', 'Portfel aktywowany'],
      ['okno', 'Otwórz nowe okno', 'Nowe okno otwarte'],
    ];
    const y = (i) => oy + COMPACT_ROWS[i];
    c.box('portfel', BOX_X, oy, BOX_W, COMPACT_ROWS[4] + S + 44, 'Portfel');
    rows.forEach(([k, cmd, ev], i) => {
      c.sticky(k, 0, y(i), 'blue', cmd);
      c.sticky('e_' + k, EV_X, y(i), 'orange', ev);
    });
    c.sticky('r_akt0', RULE_X[0], y(0), 'yellow', 'Portfel aktywny');
    c.ghost('r_osoba', RULE_X[1], y(0), 'osoba z dostępem: poza portfelem');
    c.sticky('r_saldo', RULE_X[2], y(0), 'yellow', 'Punkty nie przekraczają salda');
    c.sticky('r_okno', RULE_X[3], y(0), 'yellow', 'Bieżące okno nie jest wyczerpane');
    c.sticky('r_akt1', RULE_X[0], y(1), 'yellow', 'Portfel aktywny');

    c.arrow('zreal:r', 'r_akt0:l');
    c.arrow('r_okno:r', 'e_zreal:l');
    c.arrow('dolad:r', 'r_akt1:l');
    c.arrow('r_akt1:r', 'e_dolad:l');
    c.arrow('dezakt:r', 'e_dezakt:l');
    c.arrow('akt:r', 'e_akt:l');
    c.arrow('okno:r', 'e_okno:l');
    return { bottom: oy + COMPACT_ROWS[4] + S + 44 };
  }

  // ---------- Właścicielstwo i przypadek użycia (3.3, 3.4) ----------
  const W_CMD_X = 770, W_BOX_X = 890, W_BOX_W = 260, W_EV_X = 1190;

  function wlascicielstwo(c, oy, h) {
    c.box('wlasc', W_BOX_X, oy, W_BOX_W, h, 'Właścicielstwo');
    c.sticky('w_dodaj', W_CMD_X, oy + 52, 'blue', 'Dodaj prawo do portfela');
    c.sticky('w_zabierz', W_CMD_X, oy + 162, 'blue', 'Zabierz prawo do portfela');
    c.sticky('w_nadano', W_EV_X, oy + 52, 'orange', 'Nadano dostęp');
    c.sticky('w_zabrano', W_EV_X, oy + 162, 'orange', 'Zabrano dostęp');
    c.arrow('w_dodaj:r', 'w_nadano:l');
    c.arrow('w_zabierz:r', 'w_zabrano:l');
  }

  const USE_CASE = [
    '<span class="kw">use case</span> <span class="fn">wypłać</span>(osoba, punkty)',
    '  właścicielstwo = <span class="fn">wyciągnij</span> właścicielstwo',
    '  <span class="kw">if</span> (właścicielstwo.<span class="fn">maPrawo</span>(osoba))',
    '    portfel = <span class="fn">wyciągnij</span> portfel',
    '    portfel.<span class="fn">wypłać</span>(punkty)',
  ];
  const USE_CASE_LOCK = [
    '<span class="kw">use case</span> <span class="fn">wypłać</span>(osoba, punkty)',
    '  właścicielstwo = <span class="fn">wyciągnij</span> właścicielstwo',
    '                   <span class="hl">FOR UPDATE</span>   <span class="cm">// blokada na czas wypłaty</span>',
    '  <span class="kw">if</span> (właścicielstwo.<span class="fn">maPrawo</span>(osoba))',
    '    portfel = <span class="fn">wyciągnij</span> portfel',
    '    portfel.<span class="fn">wypłać</span>(punkty)',
  ];

  function split(c, lock) {
    const lines = lock ? USE_CASE_LOCK : USE_CASE;
    const codeH = 40 + lines.length * 25;
    c.sticky('uc_osoba', 860, 0, 'yellow', 'Osoba z dostępem do portfela');
    c.code('uc', 600, S + 26, 620, codeH, lines.join('\n'));
    c.arrow('uc_osoba:b', 'uc:t:0.42', { kind: 'dashed', k: 10 });
    const oy = S + 26 + codeH + 110;
    const { bottom } = portfelCompact(c, oy);
    wlascicielstwo(c, oy, bottom - oy);
    c.arrow('uc:b:0.3', 'portfel:t:0.55', { kind: 'code-arrow', k1: 60, k2: 60 });
    c.arrow('uc:b:0.62', 'wlasc:t', { kind: 'code-arrow', k1: 50, k2: 50 });
    return { bottom, oy };
  }

  // ---------- Strażnik portfeli firmowych (krok 2) ----------
  function straznik(c) {
    c.box('straznik', BOX_X, 0, 262, 420, 'Strażnik portfeli firmowych');
    c.sticky('s_otworz', 0, 100, 'blue', 'Otwórz portfel firmowy');
    c.sticky('s_zamknij', 0, 210, 'blue', 'Zamknij portfel firmowy');
    c.sticky('s_regula', BOX_X + 87, 140, 'yellow', 'Firma ma najwyżej 3 portfele jednocześnie');
    c.sticky('s_otwarty', BOX_X + 262 + 38, 100, 'orange', 'Portfel firmowy otwarty');
    c.sticky('s_zamkniety', BOX_X + 262 + 38, 210, 'orange', 'Portfel firmowy zamknięty');
    c.arrow('s_otwarty:l', 's_regula:r', { kind: 'dashed', k1: 30, k2: 20 });
    c.arrow('s_zamkniety:l', 's_regula:r', { kind: 'dashed', k1: 30, k2: 20 });
  }

  // ---------- definicje rysunków ----------
  const DIAGRAMS = {
    'krok1': () => {
      const c = new Canvas();
      c.big(0, 70, ['odpada', 'walidacja']);
      c.sticky('w1', 170, 0, 'yellow', 'Numer członkowski z poprawną sumą kontrolną');
      c.sticky('w2', 270, 0, 'yellow', 'Nazwisko min. 2 znaki');
      c.sticky('w3', 170, 100, 'yellow', 'Data urodzenia w przeszłości');
      c.sticky('w4', 270, 100, 'yellow', 'E-mail i telefon w poprawnym formacie');
      c.big(420, 70, ['odpadają', 'obliczenia']);
      c.sticky('o1', 600, 50, 'yellow', 'Punkty policzone zgodnie z algorytmem naliczania');
      c.big(750, 70, ['odpadają', 'procesy']);
      c.sticky('p1', 910, 50, 'yellow', 'Push tylko wtedy, gdy punkty trafiły do portfela');
      return c.svg('Krok 1: odpada walidacja, obliczenia i procesy');
    },
    'krok2': () => {
      const c = new Canvas();
      const st = new Canvas(); straznik(st);
      const pf = new Canvas(); portfelFull(pf, 0, { policy: true });
      c.merge(st, 0, 0);
      c.merge(pf, 560, 0);
      return c.svg('Krok 2: Strażnik portfeli firmowych i Portfel ze zdarzeniami, które wpływają na reguły');
    },
    'krok3-1': () => {
      const c = new Canvas();
      const { bottom } = portfelFull(c, 0, { policy: true });
      c.bubble('aga', 830, bottom - 330, 300, 140, 'Politykę ustawiamy raz, i to w nocy.', 0.55);
      c.arrow('aga:l:0.75', 'e_polityka:r', { kind: 'red', k1: 40, k2: 60 });
      return c.svg('Portfel z polityką naliczeń i odpowiedź Agi');
    },
    'krok3-2': () => {
      const c = new Canvas();
      const { bottom } = portfelFull(c, 0, { policy: false });
      c.bubble('aga', 830, 420, 290, 120, 'Zabranie prawa podczas wypłaty to nie problem.', 0.45);
      c.arrow('aga:l:0.25', 'e_dodaj:r', { kind: 'red', k1: 40, k2: 50 });
      c.arrow('aga:l:0.7', 'e_zabierz:r', { kind: 'red', k1: 40, k2: 50 });
      c.slider(BOX_X + 40, bottom + 44, BOX_W - 80, 0.18, 'spójność ++ · skalowalność −−');
      return c.svg('Portfel z dostępem w środku, odpowiedź Agi i suwak');
    },
    'krok3-3': () => {
      const c = new Canvas();
      const { bottom } = split(c, false);
      c.slider(380, bottom + 50, 480, 0.82, 'spójność −− · skalowalność ++');
      return c.svg('Portfel i Właścicielstwo osobno, przypadek użycia pyta o prawo');
    },
    'krok3-4': () => {
      const c = new Canvas();
      const { bottom, oy } = split(c, true);
      c.card('proc', 1330, oy + 300, 230, 170, 'Agregat procesowy', 'use case blokuje Właścicielstwo <code>SELECT … FOR UPDATE</code>');
      c.arrow('proc:t', 'uc:r:0.5', { kind: 'solid', k1: 120, k2: 80 });
      c.slider(380, bottom + 50, 480, 0.25, 'spójność ++ · skalowalność −−', 'elastyczność ++');
      return c.svg('Agregat procesowy: przypadek użycia blokuje Właścicielstwo na czas wypłaty');
    },
    wariantA: () => {
      const c = new Canvas();
      portfelFull(c, 0, { policy: false });
      return c.svg('Wariant A: portfel z nadaniem i odebraniem dostępu');
    },
    wariantB: () => {
      const c = new Canvas();
      portfelCompact(c, 0);
      return c.svg('Wariant B: portfel bez dostępu');
    },
  };

  const renderAll = (root = document) => {
    root.querySelectorAll('[data-diagram]').forEach((el) => {
      const make = DIAGRAMS[el.dataset.diagram];
      if (make && !el.dataset.rendered) { el.innerHTML = make(); el.dataset.rendered = '1'; }
    });
  };

  window.Diagrams = { renderAll };
})();
