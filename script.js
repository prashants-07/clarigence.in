const menuButton = document.querySelector('.menu-toggle');
const nav = document.querySelector('.nav-links');

if (menuButton && nav) {
  const navId = 'primary-navigation';
  nav.id = navId;
  menuButton.setAttribute('aria-controls', navId);

  const currentPage = window.location.pathname.split('/').pop() || 'index.html';
  nav.querySelectorAll('a[href]').forEach((link) => {
    const linkPage = link.getAttribute('href').split('#')[0];
    if (linkPage === currentPage) link.setAttribute('aria-current', 'page');
  });

  const closeMenu = (returnFocus = false) => {
    nav.classList.remove('open');
    menuButton.setAttribute('aria-expanded', 'false');
    menuButton.setAttribute('aria-label', 'Open navigation');
    if (returnFocus) menuButton.focus();
  };

  menuButton.addEventListener('click', () => {
    const isOpen = nav.classList.toggle('open');
    menuButton.setAttribute('aria-expanded', String(isOpen));
    menuButton.setAttribute('aria-label', isOpen ? 'Close navigation' : 'Open navigation');
  });

  nav.addEventListener('click', (event) => {
    if (event.target.closest('a')) {
      closeMenu();
    }
  });

  document.addEventListener('keydown', (event) => {
    if (event.key === 'Escape' && nav.classList.contains('open')) closeMenu(true);
  });

  document.addEventListener('click', (event) => {
    if (nav.classList.contains('open') && !nav.contains(event.target) && !menuButton.contains(event.target)) closeMenu();
  });

  window.addEventListener('resize', () => {
    if (window.innerWidth > 650 && nav.classList.contains('open')) closeMenu();
  });
}

const contactForm = document.querySelector('.contact-form[data-api-url]');
if (contactForm) {
  contactForm.addEventListener('submit', async (event) => {
    event.preventDefault();
    if (!contactForm.reportValidity()) return;

    const submitButton = contactForm.querySelector('[type="submit"]');
    const statusMessage = contactForm.querySelector('#form-status');
    const originalButton = submitButton.innerHTML;
    const payload = Object.fromEntries(new FormData(contactForm).entries());

    contactForm.querySelectorAll('[name]').forEach((field) => field.removeAttribute('aria-invalid'));
    submitButton.disabled = true;
    submitButton.textContent = 'Sending…';
    statusMessage.textContent = 'Sending your enquiry securely…';
    statusMessage.dataset.state = 'pending';

    try {
      const response = await fetch(contactForm.dataset.apiUrl, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
        body: JSON.stringify(payload)
      });
      const result = await response.json().catch(() => ({}));

      if (!response.ok) {
        const fields = Object.keys(result.fieldErrors || {});
        fields.forEach((name) => {
          const field = contactForm.elements.namedItem(name);
          if (field) field.setAttribute('aria-invalid', 'true');
        });
        statusMessage.textContent = fields.length
          ? `Please check: ${fields.join(', ')}.`
          : (result.message || 'We could not send your enquiry. Please try again.');
        statusMessage.dataset.state = 'error';
        if (fields.length) contactForm.elements.namedItem(fields[0])?.focus();
        return;
      }

      contactForm.reset();
      statusMessage.textContent = result.message || 'Thank you. Your enquiry has been sent.';
      statusMessage.dataset.state = 'success';
    } catch {
      statusMessage.textContent = 'We could not reach the enquiry service. Please try again or email hello@clarigence.in.';
      statusMessage.dataset.state = 'error';
    } finally {
      submitButton.disabled = false;
      submitButton.innerHTML = originalButton;
    }
  });
}

document.querySelectorAll('[data-year]').forEach((node) => {
  node.textContent = new Date().getFullYear();
});

const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
if ('IntersectionObserver' in window && !reduceMotion) {
  const revealTargets = document.querySelectorAll(
    'main > section, .service-card, .process-step, .work-card, .portfolio-item, .solution-card, .value-grid article'
  );
  revealTargets.forEach((node) => node.classList.add('reveal'));
  document.documentElement.classList.add('reveal-ready');

  const revealObserver = new IntersectionObserver((entries, observer) => {
    entries.forEach((entry) => {
      if (entry.isIntersecting) {
        entry.target.classList.add('is-visible');
        observer.unobserve(entry.target);
      }
    });
  }, { threshold: 0.12 });

  revealTargets.forEach((node) => revealObserver.observe(node));
}
