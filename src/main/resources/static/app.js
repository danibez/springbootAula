// Base da API. Vazio = mesma origem (front servido pelo próprio Spring Boot).
// Se abrir o HTML de outro lugar, troque para "http://localhost:8080".
const API = "";

const $ = (id) => document.getElementById(id);

let produtos = [];
let pedidos = [];

function log(msg, isError = false) {
  const line = document.createElement("div");
  if (isError) line.className = "err";
  line.textContent = `[${new Date().toLocaleTimeString()}] ${msg}`;
  $("log").prepend(line);
}

async function api(method, path, body) {
  const opts = { method, headers: {} };
  if (body !== undefined) {
    opts.headers["Content-Type"] = "application/json";
    opts.body = JSON.stringify(body);
  }
  log(`→ ${method} ${path}${body ? " " + JSON.stringify(body) : ""}`);
  const res = await fetch(API + path, opts);
  const text = await res.text();
  let data;
  try { data = JSON.parse(text); } catch { data = text; }
  if (!res.ok) {
    log(`← ${res.status} ${text}`, true);
    throw new Error(`HTTP ${res.status}`);
  }
  log(`← ${res.status} ${text || "(sem corpo)"}`);
  return data;
}

function esc(s) {
  return String(s ?? "").replace(/[&<>"]/g, (c) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;" }[c]));
}

const money = (v) => "R$ " + Number(v).toFixed(2);

function fillSelect(sel, items, label) {
  sel.innerHTML = items.length
    ? items.map((i) => `<option value="${i.id}">${label(i)}</option>`).join("")
    : `<option value="">(nenhum)</option>`;
}

// ---------- Clientes ----------
async function loadClientes() {
  const clientes = await api("GET", "/api/v1/cliente/all");
  $("tblClientes").innerHTML = clientes.map((c) => `
    <tr><td>${c.id}</td><td>${esc(c.username)}</td>
    <td><button onclick="excluirCliente(${c.id})">Excluir</button></td></tr>`).join("");
  fillSelect($("selCliente"), clientes, (c) => `${esc(c.username)} (#${c.id})`);
}

async function excluirCliente(id) {
  try { await api("DELETE", `/api/v1/cliente/cliente/${id}`); await reloadAll(); } catch {}
}

$("formCliente").addEventListener("submit", async (e) => {
  e.preventDefault();
  const f = e.target;
  try {
    await api("POST", "/api/v1/cliente/novoCliente", { id: 0, username: f.username.value, password: f.password.value });
    f.reset();
    await reloadAll();
  } catch {}
});

// ---------- Produtos ----------
async function loadProdutos() {
  produtos = await api("GET", "/api/v1/produto/listarProdutos");
  $("tblProdutos").innerHTML = produtos.map((p) => `
    <tr><td>${p.id}</td><td>${esc(p.nome)}</td><td>${money(p.preco)}</td><td>${p.quantidade}</td>
    <td><button onclick="editarProduto(${p.id})">Editar</button>
        <button onclick="excluirProduto(${p.id})">Excluir</button></td></tr>`).join("");
  $("chkProdutos").innerHTML = produtos.length
    ? produtos.map((p) => `<label><input type="checkbox" value="${p.id}"> ${esc(p.nome)}</label>`).join("")
    : "(cadastre produtos primeiro)";
}

function editarProduto(id) {
  const p = produtos.find((x) => x.id === id);
  const f = $("formProduto");
  f.produtoId.value = p.id;
  f.nome.value = p.nome;
  f.preco.value = p.preco;
  f.quantidade.value = p.quantidade;
  $("btnProduto").textContent = `Salvar produto #${p.id}`;
  $("btnCancelarEdicao").hidden = false;
}

function cancelarEdicao() {
  $("formProduto").reset();
  $("formProduto").produtoId.value = "";
  $("btnProduto").textContent = "Criar produto";
  $("btnCancelarEdicao").hidden = true;
}

async function excluirProduto(id) {
  try { await api("DELETE", `/api/v1/produto/produto/${id}`); await reloadAll(); } catch {}
}

$("btnCancelarEdicao").addEventListener("click", cancelarEdicao);

$("formProduto").addEventListener("submit", async (e) => {
  e.preventDefault();
  const f = e.target;
  // id: 0 é necessário: o Jackson usa o construtor e falha com "null" no `long id`
  const body = { id: 0, nome: f.nome.value, preco: Number(f.preco.value), quantidade: Number(f.quantidade.value) };
  try {
    if (f.produtoId.value) await api("PUT", `/api/v1/produto/produto/${f.produtoId.value}`, body);
    else await api("POST", "/api/v1/produto/newProduto", body);
    cancelarEdicao();
    await reloadAll();
  } catch {}
});

// ---------- Pedidos ----------
async function loadPedidos() {
  pedidos = await api("GET", "/api/pedidos");
  $("tblPedidos").innerHTML = pedidos.map((p) => `
    <tr><td>${p.id}</td><td>${new Date(p.dataPedido).toLocaleString()}</td>
    <td>${esc(p.cliente?.username)}</td>
    <td>${p.produtos.map((x) => esc(x.nome)).join(", ")}</td>
    <td>${esc(p.status)}</td>
    <td>${p.pagamento ? `#${p.pagamento.id} ${esc(p.pagamento.status)}` : "-"}</td>
    <td><button onclick="excluirPedido(${p.id})">Excluir</button></td></tr>`).join("");
  fillSelect($("selPedido"), pedidos, (p) => `Pedido #${p.id} - ${esc(p.cliente?.username)} (${money(totalPedido(p))})`);
  sugerirValor();
}

const totalPedido = (p) => p.produtos.reduce((s, x) => s + x.preco, 0);

async function excluirPedido(id) {
  try { await api("DELETE", `/api/pedidos/${id}`); await reloadAll(); } catch {}
}

$("formPedido").addEventListener("submit", async (e) => {
  e.preventDefault();
  const produtosIds = [...document.querySelectorAll("#chkProdutos input:checked")].map((c) => Number(c.value));
  try {
    await api("POST", "/api/pedidos", { clienteId: Number(e.target.clienteId.value), produtosIds });
    await reloadAll();
  } catch {}
});

// ---------- Pagamentos ----------
async function loadPagamentos() {
  const pagamentos = await api("GET", "/api/pagamentos");
  $("tblPagamentos").innerHTML = pagamentos.map((p) => `
    <tr><td>${p.id}</td><td>${money(p.valor)}</td><td>${esc(p.metodo)}</td><td>${esc(p.status)}</td>
    <td><button onclick="atualizarStatus(${p.id}, 'APROVADO')">Aprovar</button>
        <button onclick="atualizarStatus(${p.id}, 'RECUSADO')">Recusar</button>
        <button onclick="excluirPagamento(${p.id})">Excluir</button></td></tr>`).join("");
}

// Preenche o valor do pagamento com o total do pedido selecionado
function sugerirValor() {
  const p = pedidos.find((x) => x.id === Number($("selPedido").value));
  if (p) $("formPagamento").valor.value = totalPedido(p).toFixed(2);
}

$("selPedido").addEventListener("change", sugerirValor);

async function atualizarStatus(id, status) {
  try { await api("PATCH", `/api/pagamentos/${id}/status`, { status }); await reloadAll(); } catch {}
}

async function excluirPagamento(id) {
  try { await api("DELETE", `/api/pagamentos/${id}`); await reloadAll(); } catch {}
}

$("formPagamento").addEventListener("submit", async (e) => {
  e.preventDefault();
  const f = e.target;
  try {
    await api("POST", "/api/pagamentos", {
      pedidoId: Number(f.pedidoId.value),
      valor: Number(f.valor.value),
      metodo: f.metodo.value,
    });
    await reloadAll();
  } catch {}
});

// ---------- Geral ----------
async function reloadAll() {
  try {
    await loadClientes();
    await loadProdutos();
    await loadPedidos();
    await loadPagamentos();
  } catch (e) {
    log("Erro ao carregar dados: " + e.message, true);
  }
}

$("btnHello").addEventListener("click", async () => {
  try { $("hello").textContent = await api("GET", "/api/v1/cliente/hello"); } catch {}
});

$("btnReload").addEventListener("click", reloadAll);

reloadAll();
