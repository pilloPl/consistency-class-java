(() => {
  // ---------- markdown ----------
  const renderMath = (el) => {
    if (window.renderMathInElement) {
      renderMathInElement(el, { delimiters: [{ left: '$', right: '$', display: false }], throwOnError: false });
    }
  };

  const loadMd = async (el) => {
    if (el.dataset.loaded) return;
    el.dataset.loaded = '1';
    try {
      const res = await fetch(el.dataset.md);
      el.innerHTML = marked.parse(await res.text());
      renderMath(el);
      if (window.Diagrams) Diagrams.renderAll(el);
    } catch (e) {
      el.innerHTML = '<p>Nie udało się wczytać treści. <a href="' + el.dataset.md + '">Otwórz plik</a>.</p>';
    }
  };

  document.querySelectorAll('.md[data-md]:not([data-lazy])').forEach(loadMd);
  if (window.Diagrams) Diagrams.renderAll(document);

  // ---------- GitHub: wybór repo ----------
  const gh = document.querySelector('.gh-menu');
  if (gh) {
    const btn = gh.querySelector('.gh');
    btn.addEventListener('click', () => {
      const open = gh.classList.toggle('open');
      btn.setAttribute('aria-expanded', String(open));
    });
    document.addEventListener('click', (e) => {
      if (!gh.contains(e.target)) { gh.classList.remove('open'); btn.setAttribute('aria-expanded', 'false'); }
    });
  }
  document.querySelectorAll('details.full').forEach((d) => {
    d.addEventListener('toggle', () => { if (d.open) d.querySelectorAll('.md[data-md]').forEach(loadMd); });
  });

  // ---------- panels: skille / zadania ----------
  const panels = { skille: document.getElementById('skille'), zadania: document.getElementById('zadania') };
  const launchers = document.querySelectorAll('.launch-btn[data-open]');

  const openPanel = (name, scroll) => {
    Object.entries(panels).forEach(([k, p]) => { p.hidden = k !== name; });
    launchers.forEach((b) => b.setAttribute('aria-expanded', String(b.dataset.open === name)));
    if (scroll) panels[name].scrollIntoView({ behavior: 'smooth', block: 'start' });
  };

  document.querySelectorAll('[data-open]').forEach((el) => {
    el.addEventListener('click', (e) => {
      const name = el.dataset.open;
      const isOpen = !panels[name].hidden;
      if (el.classList.contains('launch-btn') && isOpen) {
        panels[name].hidden = true;
        el.setAttribute('aria-expanded', 'false');
        history.replaceState(null, '', '#');
        return;
      }
      e.preventDefault();
      openPanel(name, true);
      history.replaceState(null, '', '#' + name);
    });
  });

  const fromHash = () => {
    const h = location.hash.slice(1);
    if (panels[h]) openPanel(h, false);
    else if (/^zadanie-\d$/.test(h)) { openPanel('zadania', false); document.getElementById(h)?.scrollIntoView(); }
  };
  fromHash();

  // ---------- task content and solutions ----------
  document.querySelectorAll('[data-expand]').forEach((btn) => {
    btn.addEventListener('click', () => {
      const md = btn.closest('.task').querySelector('.clamp');
      const open = md.classList.toggle('open');
      btn.textContent = open ? 'Zwiń treść' : 'Rozwiń treść';
      if (!open) md.scrollIntoView({ behavior: 'smooth', block: 'center' });
    });
  });

  document.querySelectorAll('[data-solution]').forEach((btn) => {
    btn.setAttribute('aria-expanded', 'false');
    btn.addEventListener('click', () => {
      const sol = document.getElementById(btn.dataset.solution);
      sol.hidden = !sol.hidden;
      btn.setAttribute('aria-expanded', String(!sol.hidden));
      btn.textContent = sol.hidden ? 'Pokaż rozwiązanie' : 'Ukryj rozwiązanie';
      if (!sol.hidden) sol.scrollIntoView({ behavior: 'smooth', block: 'start' });
    });
  });

  // ---------- lightbox ----------
  const lb = document.getElementById('lightbox');
  const lbBody = lb.querySelector('.lb-body');
  document.addEventListener('click', (e) => {
    const z = e.target.closest('.zoom, .diagram');
    if (!z) return;
    if (z.classList.contains('diagram')) {
      const svg = z.querySelector('svg');
      if (!svg) return;
      lbBody.innerHTML = '';
      lbBody.appendChild(svg.cloneNode(true));
      lb.querySelector('.lb-cap').textContent = svg.getAttribute('aria-label') || '';
      lb.classList.add('is-diagram');
    } else {
      lbBody.innerHTML = '<img src="' + z.dataset.src + '" alt="">';
      lb.querySelector('.lb-cap').textContent = z.dataset.caption || '';
      lb.classList.remove('is-diagram');
    }
    lb.showModal();
  });
  lb.addEventListener('click', (e) => { if (!e.target.closest('.lb-close') && e.target.closest('.lb-body svg')) return; lb.close(); });

  // ---------- mind map ----------
  const svg = document.getElementById('mindmap');
  if (!window.markmap || !svg) return;
  const { Transformer, Markmap } = window.markmap;
  const { root } = new Transformer().transform(document.getElementById('mindmap-md').textContent.trim());

  const branchColors = ['#4262ff', '#ff8a00', '#e8b100', '#00b5a6', '#e5487a', '#7b61ff'];
  const color = (node) => {
    const path = (node.state && node.state.path) || '';
    const parts = path.split('.');
    if (parts.length < 2) return '#050038';
    return branchColors[(parseInt(parts[1], 10) - 1) % branchColors.length];
  };

  const mm = Markmap.create(svg, {
    color,
    maxWidth: 250,
    paddingX: 12,
    spacingVertical: 10,
    spacingHorizontal: 60,
    duration: 350,
    initialExpandLevel: -1,
    autoFit: false,
  }, root);
  setTimeout(() => mm.fit(), 60);

  document.getElementById('mm-fit').addEventListener('click', () => mm.fit());

  let collapsed = false;
  const toggleBtn = document.getElementById('mm-toggle');
  toggleBtn.addEventListener('click', () => {
    collapsed = !collapsed;
    const walk = (n, depth) => {
      n.payload = { ...(n.payload || {}), fold: collapsed && depth >= 1 ? 1 : 0 };
      (n.children || []).forEach((c) => walk(c, depth + 1));
    };
    walk(mm.state.data, 0);
    mm.renderData().then(() => mm.fit());
    toggleBtn.textContent = collapsed ? 'Rozwiń gałęzie' : 'Zwiń gałęzie';
  });

  window.addEventListener('resize', () => mm.fit());
})();
