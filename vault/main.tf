variable "cidrs" {}

module "approle-setup" {
  name = "template-service"
  cidrs = "${var.cidrs}"
  source = "github.com/Banno/env-setup//modules/vault-approle-setup?ref=cc5d4ec67ae09ef3fa139b3cb1c3f4c577eda31f"
}

resource "vault_policy" "policy" {
  name = "template-service"
  policy = "${file("${path.module}/policy.hcl")}"
}
