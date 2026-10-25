import { MaterialForm } from "@/components/material-form";
import { PageHeader } from "@/components/ui";
export default function NovoMaterialPage() {
  return (
    <>
      <PageHeader
        eyebrow="Nova doação"
        title="Cadastrar material"
        description="Preencha as informações para encontrar o melhor destino."
      />
      <MaterialForm />
    </>
  );
}
