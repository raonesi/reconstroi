/* As imagens são arquivos enviados em runtime e servidos pela API. */
/* eslint-disable @next/next/no-img-element */
"use client";
import Link from "next/link";
import { MapPin, PackageOpen, Ruler } from "lucide-react";
import { assetUrl } from "@/lib/api";
import { conservacaoLabels, type Material } from "@/lib/types";
import { StatusBadge } from "./ui";

export function MaterialCard({ material }: { material: Material }) {
  const image = material.fotos?.[0]?.url;
  return (
    <article className="material-card">
      <Link href={`/materiais/${material.id}`} className="material-image">
        {image ? (
          <img src={assetUrl(image)} alt={material.nome} />
        ) : (
          <div className="image-placeholder">
            <PackageOpen />
            <span>Imagem em breve</span>
          </div>
        )}
        <StatusBadge status={material.status} />
      </Link>
      <div className="material-body">
        <span className="category-chip">{material.categoria.nome}</span>
        <h3>
          <Link href={`/materiais/${material.id}`}>{material.nome}</Link>
        </h3>
        <p>{material.descricao}</p>
        <div className="material-meta">
          <span>
            <Ruler /> {material.quantidade} {material.unidade}
          </span>
          <span>
            <MapPin /> {material.cidade}/{material.estado}
          </span>
        </div>
        <div className="card-footer">
          <small>{conservacaoLabels[material.estadoConservacao]}</small>
          <Link href={`/materiais/${material.id}`}>Ver detalhes →</Link>
        </div>
      </div>
    </article>
  );
}
