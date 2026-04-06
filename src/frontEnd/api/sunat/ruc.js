export default async function handler(req, res) {
  if (req.method !== "GET") {
    res.setHeader("Allow", "GET");
    return res.status(405).json({ error: "Method Not Allowed" });
  }

  const numero = typeof req.query?.numero === "string" ? req.query.numero.trim() : "";
  if (!numero) {
    return res.status(400).json({ error: "Falta el parámetro 'numero'" });
  }

  const headerToken =
    typeof req.headers?.["x-sunat-token"] === "string"
      ? req.headers["x-sunat-token"].trim()
      : "";
  const queryToken = typeof req.query?.token === "string" ? req.query.token.trim() : "";
  const token = process.env.SUNAT_TOKEN || process.env.VITE_SUNAT_TOKEN || headerToken || queryToken;
  if (!token) {
    return res.status(500).json({ error: "SUNAT token no configurado" });
  }

  const upstreamUrl = `https://api.apis.net.pe/v1/ruc?numero=${encodeURIComponent(numero)}`;

  try {
    const upstream = await fetch(upstreamUrl, {
      headers: {
        Accept: "application/json",
        Authorization: `Bearer ${token}`,
      },
    });

    const contentType = upstream.headers.get("content-type") || "application/json";
    const bodyText = await upstream.text();

    res.setHeader("Content-Type", contentType);
    return res.status(upstream.status).send(bodyText);
  } catch (error) {
    console.error("sunat proxy error", error);
    return res.status(502).json({ error: "Error consultando SUNAT" });
  }
}
