"use client";

import { useEffect } from "react";
import { setActiveGroup } from "@/app/actions/groups";

interface Props {
  urlGroupId: string | undefined;
  cookieGroupId: string | undefined;
}

export default function GroupSync({ urlGroupId, cookieGroupId }: Props) {
  useEffect(() => {
    // Si tenim un groupId per URL i és diferent del que tenim a la cookie,
    // el persistim fent servir l'Acció de Servidor (que sí pot modificar cookies).
    if (urlGroupId && urlGroupId !== cookieGroupId) {
      setActiveGroup(urlGroupId);
    }
  }, [urlGroupId, cookieGroupId]);

  return null;
}
