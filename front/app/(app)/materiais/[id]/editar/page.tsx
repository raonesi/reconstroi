"use client";
import { useParams } from "next/navigation";
import { useEffect, useState } from "react";
import { api } from "@/lib/api";
import type { Material } from "@/lib/types";
import { MaterialForm } from "@/components/material-form";
import { PageHeader, Spinner } from "@/components/ui";
export default function EditarMaterialPage() {
  const { id } = useParams<{ id: string }>();
  const [material, setMaterial] = useState<Material>();
  useEffect(() => {
    api.material(id).then(setMaterial);
  }, [id]);
  if (!material) return <Spinner />;
  return (
    <>
      <PageHeader
        eyebrow="Gerenciar anúncio"
        title="Editar material"
        description="Mantenha as informações atualizadas para facilitar a retirada."
      />
      <MaterialForm material={material} />
    </>
  );
}
