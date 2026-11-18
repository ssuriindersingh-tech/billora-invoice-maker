
let invoices = JSON.parse(localStorage.getItem("billoraInvoices") || "[]");

const money = amount => "₹" + Number(amount).toFixed(2);

function addItem() {
  const row = document.createElement("div");
  row.className = "item-row";

  row.innerHTML = `
    <input class="description" placeholder="Product or service">
    <input class="qty" type="number" min="0" value="1" oninput="calculate()">
    <input class="price" type="number" min="0" value="0" oninput="calculate()">
    <span class="line-total">₹0.00</span>
    <button type="button" onclick="this.parentElement.remove(); calculate()">×</button>
  `;

  document.getElementById("items").appendChild(row);
  calculate();
}

function calculate() {
  let subtotal = 0;

  document.querySelectorAll(".item-row").forEach(row => {
    const qty = Number(row.querySelector(".qty").value) || 0;
    const price = Number(row.querySelector(".price").value) || 0;
    const total = qty * price;

    row.querySelector(".line-total").textContent = money(total);
    subtotal += total;
  });

  const taxRate = Number(document.getElementById("tax").value) || 0;
  const taxAmount = subtotal * taxRate / 100;
  const total = subtotal + taxAmount;

  document.getElementById("subtotal").textContent = money(subtotal);
  document.getElementById("taxAmount").textContent = money(taxAmount);
  document.getElementById("grandTotal").textContent = money(total);

  return { subtotal, taxAmount, total };
}

function showDashboard() {
  document.getElementById("dashboard").classList.remove("hidden");
  document.getElementById("invoicePage").classList.add("hidden");
  document.getElementById("historyPage").classList.add("hidden");
  updateDashboard();
}

function showInvoice() {
  document.getElementById("dashboard").classList.add("hidden");
  document.getElementById("invoicePage").classList.remove("hidden");
  document.getElementById("historyPage").classList.add("hidden");

  document.getElementById("invoiceNumber").value =
    "INV-" + Date.now().toString().slice(-6);

  document.getElementById("invoiceDate").value =
    new Date().toISOString().slice(0, 10);

  document.getElementById("dueDate").value = "";
  document.getElementById("businessName").value = "";
  document.getElementById("customerName").value = "";
  document.getElementById("customerEmail").value = "";
  document.getElementById("tax").value = "0";
  document.getElementById("items").innerHTML = "";

  addItem();
}

function saveInvoice() {
  const business = document.getElementById("businessName").value.trim();
  const customer = document.getElementById("customerName").value.trim();

  if (!business || !customer) {
    alert("Please enter business name and customer name.");
    return;
  }

  const items = [...document.querySelectorAll(".item-row")].map(row => ({
    description: row.querySelector(".description").value,
    qty: Number(row.querySelector(".qty").value) || 0,
    price: Number(row.querySelector(".price").value) || 0
  }));

  if (!items.length || items.some(item => !item.description.trim() || item.qty <= 0 || item.price < 0)) {
    alert("Please add valid invoice items.");
    return;
  }

  const totals = calculate();

  const invoice = {
    id: document.getElementById("invoiceNumber").value,
    business,
    customer,
    email: document.getElementById("customerEmail").value,
    date: document.getElementById("invoiceDate").value,
    dueDate: document.getElementById("dueDate").value,
    items,
    tax: Number(document.getElementById("tax").value) || 0,
    ...totals
  };

  if (invoices.some(item => item.id === invoice.id)) {
    alert("This invoice number already exists.");
    return;
  }

  invoices.unshift(invoice);
  localStorage.setItem("billoraInvoices", JSON.stringify(invoices));

  alert("Invoice saved successfully!");
  showDashboard();
}

function updateDashboard() {
  document.getElementById("totalInvoices").textContent = invoices.length;

  const revenue = invoices.reduce((sum, invoice) => sum + invoice.total, 0);
  document.getElementById("totalRevenue").textContent = money(revenue);

  const customers = new Set(invoices.map(i => i.customer));
  document.getElementById("totalCustomers").textContent = customers.size;

  const recent = document.getElementById("recentInvoices");

  recent.innerHTML = invoices.slice(0, 5).map(invoice => `
    <div class="invoice-entry">
      <span>${escapeHTML(invoice.id)} – ${escapeHTML(invoice.customer)}</span>
      <strong>${money(invoice.total)}</strong>
    </div>
  `).join("") || '<p class="empty">No invoices yet. Create your first invoice!</p>';
}

function showHistory() {
  document.getElementById("dashboard").classList.add("hidden");
  document.getElementById("invoicePage").classList.add("hidden");
  document.getElementById("historyPage").classList.remove("hidden");

  const list = document.getElementById("historyList");

  list.innerHTML = invoices.map((invoice, index) => `
    <div class="invoice-entry">
      <div>
        <strong>${escapeHTML(invoice.id)}</strong><br>
        ${escapeHTML(invoice.customer)}
      </div>
      <div>
        <strong>${money(invoice.total)}</strong><br>
        <button class="secondary" onclick="printSavedInvoice(${index})">Print PDF</button>
      </div>
    </div>
  `).join("") || '<p class="empty">No invoices saved.</p>';
}

function escapeHTML(value) {
  return String(value).replace(/[&<>"']/g, char => ({
    "&": "&amp;",
    "<": "&lt;",
    ">": "&gt;",
    '"': "&quot;",
    "'": "&#39;"
  })[char]);
}

function printDocument(invoice) {
  const rows = invoice.items.map(item => `
    <tr>
      <td>${escapeHTML(item.description)}</td>
      <td>${item.qty}</td>
      <td>${money(item.price)}</td>
      <td>${money(item.qty * item.price)}</td>
    </tr>
  `).join("");

  const win = window.open("", "_blank");

  if (!win) {
    alert("Please allow pop-ups to print the invoice.");
    return;
  }

  win.document.write(`
    <!DOCTYPE html>
    <html>
    <head>
      <title>Invoice ${escapeHTML(invoice.id)}</title>
      <style>
        body { font-family: Arial; padding: 40px; color: #18243a; }
        h1 { color: #b8862c; }
        table { width: 100%; border-collapse: collapse; margin-top: 30px; }
        th, td { border-bottom: 1px solid #ddd; padding: 12px; text-align: left; }
        .total { text-align: right; margin-top: 30px; }
        @media print { button { display:none; } }
      </style>
    </head>
    <body>
      <h1>${escapeHTML(invoice.business)}</h1>
      <h2>INVOICE</h2>
      <p>Invoice: ${escapeHTML(invoice.id)}</p>
      <p>Date: ${escapeHTML(invoice.date)}</p>
      <p>Due: ${escapeHTML(invoice.dueDate || "Not specified")}</p>
      <h3>Bill To: ${escapeHTML(invoice.customer)}</h3>
      <p>${escapeHTML(invoice.email || "")}</p>
      <table>
        <tr><th>Description</th><th>Qty</th><th>Price</th><th>Total</th></tr>
        ${rows}
      </table>
      <div class="total">
        <p>Subtotal: ${money(invoice.subtotal)}</p>
        <p>Tax (${invoice.tax}%): ${money(invoice.taxAmount)}</p>
        <h2>Grand Total: ${money(invoice.total)}</h2>
      </div>
      <button onclick="window.print()">Print / Save as PDF</button>
    </body>
    </html>
  `);

  win.document.close();
}

function downloadInvoice() {
  const business = document.getElementById("businessName").value.trim();
  const customer = document.getElementById("customerName").value.trim();

  if (!business || !customer) {
    alert("Enter business and customer name first.");
    return;
  }

  const totals = calculate();

  const invoice = {
    id: document.getElementById("invoiceNumber").value,
    business,
    customer,
    email: document.getElementById("customerEmail").value,
    date: document.getElementById("invoiceDate").value,
    dueDate: document.getElementById("dueDate").value,
    tax: Number(document.getElementById("tax").value) || 0,
    items: [...document.querySelectorAll(".item-row")].map(row => ({
      description: row.querySelector(".description").value,
      qty: Number(row.querySelector(".qty").value) || 0,
      price: Number(row.querySelector(".price").value) || 0
    })),
    ...totals
  };

  printDocument(invoice);
}

function printSavedInvoice(index) {
  printDocument(invoices[index]);
}

addItem();
updateDashboard();
