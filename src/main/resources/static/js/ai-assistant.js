// AI Assistant — a rule-based (pattern-matching) knowledge assistant.
// It runs fully client-side with no external API calls, so it works
// reliably offline/during a demo. The knowledge base below covers common
// blood-donation questions and how this app's own features work.

function toggleAiAssistant() {
    var panel = document.getElementById('ai-assistant-panel');
    panel.classList.toggle('open');
}

var AI_KNOWLEDGE_BASE = [
    {
        keywords: ['eligib', 'who can donate', 'age', 'weight'],
        answer: "You can donate blood if you're 18–65 years old, weigh at least 45kg, and are in general good health. On this site, you also need to wait at least 90 days between donations — your dashboard shows your exact next-eligible date automatically."
    },
    {
        keywords: ['how often', 'gap', 'wait', 'frequency'],
        answer: "The standard gap between whole-blood donations is about 90 days (roughly 3 months), to let your body fully replenish. This app enforces that automatically once you log a donation."
    },
    {
        keywords: ['match', 'matching', 'compatib', 'ai'],
        answer: "Our matching engine checks real blood-compatibility rules, not just an exact blood-group match. For example, O- is the universal donor and can give to any group, while AB+ can receive from any group. The Find Donors page shows compatible donor groups ranked by match strength."
    },
    {
        keywords: ['register', 'sign up', 'account', 'donor account'],
        answer: "Go to the Register page, choose your own username and password, and fill in your name, blood group, city and phone. That creates both your donor profile and your login in one step."
    },
    {
        keywords: ['emergency', 'contact', 'helpline', 'urgent', 'need blood now'],
        answer: "For a real emergency, call 108 (national ambulance) or use the Request Blood page to notify eligible donors near you instantly. The Contact page also lists verified national blood-service helplines."
    },
    {
        keywords: ['request blood', 'raise a request', 'need donor'],
        answer: "Use the Request Blood page — no login needed. Fill in the blood group, city, hospital and units required, and it goes straight into the system for matching."
    },
    {
        keywords: ['hospital', 'nearby', 'map'],
        answer: "The Hospitals page has an interactive map of major hospitals and blood banks across India — tap any pin for details."
    },
    {
        keywords: ['admin', 'stock', 'inventory'],
        answer: "Admins manage blood bank stock levels and open requests from the Admin Dashboard, accessible after logging in with an admin account."
    },
    {
        keywords: ['benefit', 'why donate', 'health benefit'],
        answer: "Donating blood is safe and takes about 30 minutes. Regular donors may also see health benefits like better cardiovascular monitoring during screening. Most importantly — one donation can help save up to three lives."
    },
    {
        keywords: ['password', 'forgot', 'reset'],
        answer: "There's no self-service password reset yet in this version — please contact the site admin directly if you're locked out."
    }
];

function askAi(question) {
    document.getElementById('ai-assistant-input').value = question;
    sendAiMessage();
}

function sendAiMessage() {
    var input = document.getElementById('ai-assistant-input');
    var text = input.value.trim();
    if (!text) return;

    appendAiMessage(text, 'user');
    input.value = '';

    var lower = text.toLowerCase();
    var reply = "I'm not sure about that one yet — try asking about eligibility, donor matching, emergency contacts, or how to register.";

    for (var i = 0; i < AI_KNOWLEDGE_BASE.length; i++) {
        var entry = AI_KNOWLEDGE_BASE[i];
        for (var k = 0; k < entry.keywords.length; k++) {
            if (lower.indexOf(entry.keywords[k]) !== -1) {
                reply = entry.answer;
                break;
            }
        }
        if (reply !== "I'm not sure about that one yet — try asking about eligibility, donor matching, emergency contacts, or how to register.") break;
    }

    setTimeout(function () { appendAiMessage(reply, 'bot'); }, 350);
}

function appendAiMessage(text, sender) {
    var container = document.getElementById('ai-assistant-messages');
    var div = document.createElement('div');
    div.className = 'ai-msg ' + sender;
    div.textContent = text;
    container.appendChild(div);
    container.scrollTop = container.scrollHeight;
}
