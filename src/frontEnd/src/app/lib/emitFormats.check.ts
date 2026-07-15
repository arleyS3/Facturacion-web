import assert from "node:assert/strict";

import { countSelectedFormats, getSelectableEmitFormats } from "./emitFormats.ts";

assert.equal(
  getSelectableEmitFormats("shipping", { xml: true, json: true, txt: true }).json,
  false,
);
assert.equal(
  getSelectableEmitFormats("sales", { xml: true, json: true, txt: true }).json,
  false,
);

assert.deepEqual(getSelectableEmitFormats("shipping", { xml: true, json: true, txt: true }), {
  xml: true,
  json: false,
  txt: true,
});

assert.deepEqual(getSelectableEmitFormats("sales", { xml: true, json: true, txt: true }), {
  xml: false,
  json: false,
  txt: true,
});

assert.equal(
  countSelectedFormats(getSelectableEmitFormats("sales", { xml: true, json: true, txt: true })),
  1,
);
