"""Export reviewed existing content for first-install CMS initialization only.

Does not connect to a database or create admin accounts. Never run at build/startup.
"""
import json
import runpy
from pathlib import Path
root=Path(__file__).resolve().parent.parent
existing=runpy.run_path(str(root/'scripts/generate-pages.py'))
services=[dict(name=s[1],slug=s[0],shortDescription=s[3],description=s[4],icon=s[2],imageUrl='',displayOrder=i*10,active=True,capabilities=s[5].split('|'),benefits=[s[3]],useCases=s[6]) for i,s in enumerate(existing['SERVICES'],1)]
documents={
 'home':dict(headline='Grow Your\nBusiness With\nTechnology.',subtitle='From your first website to smarter business systems, Clarigence helps you build, improve and grow your digital presence.',primaryCtaText='Let’s discuss your project',primaryCtaUrl='contact.html',secondaryCtaText='Explore services',secondaryCtaUrl='services.html',aboutHeading='Technology with\na business purpose.',aboutText='We help startups and growing businesses turn their requirements into useful websites, apps and connected systems. Our approach brings clear thinking, thoughtful design and practical development together.',ctaHeading='Your ambition.\nOur technology.',ctaDescription='Start with a conversation. We’ll help you find the right way forward.'),
 'about':dict(heading='Clear thinking.\nUseful technology.',intro='We help businesses establish, improve and grow their digital presence with solutions that serve a real purpose.',companyDescription='Clarigence.in is an IT services and digital solutions company. We bring websites, mobile applications, Salesforce, digital marketing and business automation together around your requirements.',mission='',vision='',valuesIntro='Practical solutions for startups and SMEs, with the attention your business deserves.'),
 'business':dict(name='Clarigence.in',tagline='Grow Your Business With Technology.',email='hello@clarigence.in',phone='',showPhone='false',whatsapp='918459179752',address='India',instagram='https://www.instagram.com/clarigence.in/',linkedin='https://www.linkedin.com/company/clarigence-in/',facebook='',hours='')
}
for filename,(title,description,body) in existing['CONTENT'].items():
 key='home' if filename=='index.html' else filename.removesuffix('.html')
 documents['seo-'+key]=dict(title=title,description=description,ogTitle=title,ogDescription=description,ogImage='assets/images/clarigence-logo.png')
(root/'backend/src/main/resources/cms-seed.json').write_text(json.dumps(dict(services=services,documents=documents),ensure_ascii=False,indent=2),encoding='utf8')
print('Exported ten existing services and page/settings defaults. No portfolio clients were invented or seeded.')
