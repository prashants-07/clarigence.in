const menuButton = document.querySelector('.menu-toggle');
const nav = document.querySelector('.nav-links');
if (menuButton && nav) {
  const closeMenu = (returnFocus = false) => {
    nav.classList.remove('open');
    menuButton.setAttribute('aria-expanded', 'false');
    menuButton.setAttribute('aria-label', 'Open navigation');
    if (returnFocus) menuButton.focus();
  };
  menuButton.addEventListener('click', () => {
    const open = nav.classList.toggle('open');
    menuButton.setAttribute('aria-expanded', String(open));
    menuButton.setAttribute('aria-label', open ? 'Close navigation' : 'Open navigation');
  });
  nav.addEventListener('click', event => { if (event.target.closest('a')) closeMenu(); });
  document.addEventListener('keydown', event => {
    if (event.key === 'Escape' && nav.classList.contains('open')) closeMenu(true);
  });
  document.addEventListener('click', event => {
    if (!nav.contains(event.target) && !menuButton.contains(event.target)) closeMenu();
  });
  document.addEventListener('focusin', event => {
    if (!nav.contains(event.target) && !menuButton.contains(event.target)) closeMenu();
  });
  window.addEventListener('resize', () => { if (window.innerWidth > 900) closeMenu(); });
}

document.querySelectorAll('.contact-form').forEach(form => {
  const status = form.querySelector('#form-status');
  const fields = [...form.querySelectorAll('[name]')];
  const messages = {
    name: 'Enter your name (up to 120 characters).',
    email: 'Enter a valid email address (up to 254 characters).',
    phone: 'Use 7–30 characters: digits, spaces, +, parentheses, dots or hyphens.',
    company: 'Use up to 160 characters for the company name.',
    service: 'Please choose a service.',
    message: 'Enter a message between 10 and 5,000 characters.'
  };
  const setFieldError = (field, message = '') => {
    const error = form.querySelector(`#${field.name}-error`);
    if (error) error.textContent = message;
    if (message) field.setAttribute('aria-invalid', 'true');
    else field.removeAttribute('aria-invalid');
  };
  const notify = (message, state) => {
    status.textContent = message;
    status.dataset.state = state;
  };
  const isValid = field => {
    const min = Number(field.getAttribute('minlength') || 0);
    const max = Number(field.getAttribute('maxlength') || Infinity);
    return field.checkValidity() && field.value.length >= min && field.value.length <= max;
  };
  fields.forEach(field => {
    field.addEventListener('input', () => setFieldError(field));
    field.addEventListener('change', () => setFieldError(field));
  });
  const requested = new URLSearchParams(location.search).get('service');
  if (requested) {
    const match = [...form.elements.service.options].find(option =>
      option.textContent.toLowerCase().replaceAll(' ', '-') === requested);
    if (match) form.elements.service.value = match.value;
  }
  form.noValidate = true;
  form.addEventListener('submit', async event => {
    event.preventDefault();
    const button = form.querySelector('[type=submit]');
    if (button.disabled) return;
    fields.forEach(field => {
      if (field.type !== 'select-one') field.value = field.value.trim();
      setFieldError(field, isValid(field) ? '' : messages[field.name]);
    });
    const invalid = fields.find(field => !isValid(field));
    if (invalid) {
      notify('Please check the highlighted fields.', 'error');
      invalid.focus();
      return;
    }
    const original = button.innerHTML;
    button.disabled = true;
    button.textContent = 'Sending enquiry…';
    form.setAttribute('aria-busy', 'true');
    notify('Sending your enquiry…', 'pending');
    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), 20000);
    try {
      const endpoint = window.CLARIGENCE_CONFIG?.contactApiUrl;
      if (!endpoint) throw new Error('Missing contact configuration');
      const response = await fetch(endpoint, {
        method: 'POST',
        headers: {'Content-Type': 'application/json', 'Accept': 'application/json'},
        body: JSON.stringify(Object.fromEntries(new FormData(form))),
        signal: controller.signal
      });
      const result = await response.json().catch(() => null);
      if (!response.ok) {
        const names = response.status === 400
          ? fields.filter(field => Object.hasOwn(result?.fieldErrors || {}, field.name)) : [];
        names.forEach(field => setFieldError(field, messages[field.name]));
        notify(names.length ? 'Please check the highlighted fields.' :
          'We couldn’t send your enquiry right now. Please try again or email clarigence@gmail.com.', 'error');
        if (names.length) names[0].focus();
        return;
      }
      if (response.status !== 201 || !result) throw new Error('Unexpected response');
      form.reset();
      notify('Thank you. Your enquiry has been sent. We’ll be in touch to discuss your project.', 'success');
      status.focus();
    } catch {
      notify('We couldn’t confirm your enquiry. Please try again later or email clarigence@gmail.com.', 'error');
    } finally {
      clearTimeout(timeout);
      button.disabled = false;
      button.innerHTML = original;
      form.removeAttribute('aria-busy');
    }
  });
});

document.querySelectorAll('[data-year]').forEach(node => { node.textContent = new Date().getFullYear(); });
const motionPreference = window.matchMedia('(prefers-reduced-motion: reduce)');
if ('IntersectionObserver' in window && !motionPreference.matches) {
  const observer = new IntersectionObserver(entries => entries.forEach(entry => {
    if (entry.isIntersecting) {
      entry.target.classList.remove('reveal-pending');
      observer.unobserve(entry.target);
    }
  }), {threshold: 0.05});
  document.querySelectorAll('.service-card, .value-grid article, .work-card, .solution-card, .process-grid article').forEach(node => {
    node.classList.add('reveal');
    if (node.getBoundingClientRect().top > window.innerHeight) node.classList.add('reveal-pending');
    observer.observe(node);
  });
  motionPreference.addEventListener('change', event => {
    if (event.matches) {
      document.querySelectorAll('.reveal-pending').forEach(node => node.classList.remove('reveal-pending'));
      observer.disconnect();
    }
  });
}
